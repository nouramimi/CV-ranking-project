import pandas as pd
import numpy as np
import re
from datetime import datetime
from collections import Counter
import difflib
from typing import List, Dict, Set, Tuple, Any

# NLP Libraries
import spacy
from spacy.matcher import Matcher
from fuzzywuzzy import fuzz, process
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem import WordNetLemmatizer

# Database
import psycopg2
from psycopg2 import sql

# Download NLTK data
try:
    nltk.data.find('tokenizers/punkt')
except LookupError:
    nltk.download('punkt')

try:
    nltk.data.find('corpora/stopwords')
except LookupError:
    nltk.download('stopwords')

try:
    nltk.data.find('corpora/wordnet')
except LookupError:
    nltk.download('wordnet')

class CVDataProcessor:
    """
    A comprehensive CV data processor that standardizes and enriches CV information.
    Now with PostgreSQL integration for job offer requirements.
    """

    def __init__(self, db_config=None):
        try:
            self.nlp = spacy.load("en_core_web_sm")
            self.db_config = db_config  # Store DB config for PostgreSQL connection
        except OSError:
            print("Please install spaCy English model: python -m spacy download en_core_web_sm")
            raise

        self.lemmatizer = WordNetLemmatizer()
        self.stop_words = set(stopwords.words('english'))

        # Skills mapping dictionary
        self.skills_mapping = {
            'js': 'JavaScript', 'javascript': 'JavaScript',
            'py': 'Python', 'python': 'Python',
            'java': 'Java', 'c++': 'C++', 'cpp': 'C++',
            'c#': 'C#', 'csharp': 'C#', 'html': 'HTML',
            'css': 'CSS', 'php': 'PHP', 'typescript': 'TypeScript',
            'ts': 'TypeScript', 'go': 'Go', 'golang': 'Go',
            'react': 'React', 'angular': 'Angular', 'vue': 'Vue.js',
            'node': 'Node.js', 'express': 'Express.js',
            'django': 'Django', 'flask': 'Flask', 'spring': 'Spring',
            'sql': 'SQL', 'mysql': 'MySQL', 'postgresql': 'PostgreSQL',
            'mongodb': 'MongoDB', 'redis': 'Redis',
            'git': 'Git', 'docker': 'Docker', 'kubernetes': 'Kubernetes',
            'aws': 'AWS', 'azure': 'Microsoft Azure', 'gcp': 'Google Cloud',
            'machine learning': 'Machine Learning', 'ml': 'Machine Learning',
            'ai': 'Artificial Intelligence', 'tensorflow': 'TensorFlow',
            'pytorch': 'PyTorch', 'data science': 'Data Science'
        }

    def load_data(self, csv_file_path: str) -> pd.DataFrame:
        """Load the CV data from CSV file."""
        try:
            df = pd.read_csv(csv_file_path)
            print(f"Loaded {len(df)} records from {csv_file_path}")
            
            # Check for required columns - adjust based on your actual CSV structure
            required_columns = ['job_offer_id']  # Only require the essential column
            for col in required_columns:
                if col not in df.columns:
                    raise ValueError(f"Input CSV is missing required column: {col}")
                    
            return df
        except Exception as e:
            print(f"Error loading data: {e}")
            raise

    def standardize_skills(self, skills_text: str) -> str:
        """Standardize skills using the mapping dictionary and fuzzy matching."""
        if pd.isna(skills_text) or not skills_text.strip():
            return ""

        skills_text = str(skills_text).lower()
        skills_text = re.sub(r'compétences identifiées:|technical skills:|skills:|compétences:', '', skills_text)
        skills = re.split(r'[,;•\n\r\t\|]+', skills_text)

        standardized_skills = set()

        for skill in skills:
            skill = skill.strip()
            if not skill or len(skill) < 2:
                continue

            skill = re.sub(r'\([^)]*\)', '', skill).strip()

            if skill in self.skills_mapping:
                standardized_skills.add(self.skills_mapping[skill])
            else:
                best_match = process.extractOne(skill, self.skills_mapping.keys(), score_cutoff=85)
                if best_match:
                    standardized_skills.add(self.skills_mapping[best_match[0]])
                else:
                    if len(skill) > 1:
                        standardized_skills.add(skill.title())

        return ', '.join(sorted(standardized_skills))

    def remove_duplicate_skills(self, skills_text: str) -> str:
        """Remove duplicate skills using fuzzy matching."""
        if pd.isna(skills_text) or not skills_text.strip():
            return ""

        skills = [s.strip() for s in str(skills_text).split(',') if s.strip()]
        unique_skills = []

        for skill in skills:
            is_duplicate = False
            for existing_skill in unique_skills:
                if fuzz.ratio(skill.lower(), existing_skill.lower()) > 85:
                    is_duplicate = True
                    break

            if not is_duplicate:
                unique_skills.append(skill)

        return ', '.join(unique_skills)

    def normalize_date_ranges(self, text: str) -> str:
        """Normalize date ranges to a standard format."""
        if pd.isna(text) or not text.strip():
            return ""

        text = str(text)
        patterns = [
            (r'(\d{2})/(\d{4})\s*[-–—]\s*(\d{2})/(\d{4})', r'\2-\1 to \4-\3'),
            (r'(\d{4})/(\d{2})\s*[-–—]\s*(\d{4})/(\d{2})', r'\1-\2 to \3-\4'),
            (r'(\d{4})\s*[-–—]\s*(\d{4})', r'\1 to \2'),
            (r'(\d{2})/(\d{4})\s*[-–—]\s*(present|now|current)', r'\2-\1 to Present'),
            (r'(\d{4})\s*[-–—]\s*(present|now|current)', r'\1 to Present'),
        ]

        normalized_text = text
        for pattern, replacement in patterns:
            normalized_text = re.sub(pattern, replacement, normalized_text, flags=re.IGNORECASE)

        return normalized_text

    def compute_total_experience_years(self, experience_text: str) -> float:
        """Compute total years of experience from experience text."""
        if pd.isna(experience_text) or not experience_text.strip():
            return 0.0

        experience_text = str(experience_text)

        # Direct year patterns - more comprehensive
        direct_patterns = [
            r'(\d+)\s*(?:years?|yrs?)\s*(?:of\s*)?(?:experience|exp)',
            r'(\d+)\+?\s*(?:years?|yrs?)',
            r'experience:\s*(\d+)\s*(?:years?|yrs?)',
            r'(\d+)\s*(?:years?|yrs?)\s*in',
            r'(\d+)\s*(?:years?|yrs?)\s*(?:work|professional)',
        ]

        for pattern in direct_patterns:
            match = re.search(pattern, experience_text, re.IGNORECASE)
            if match:
                years = float(match.group(1))
                print(f"  Found direct experience pattern: {years} years")
                return years

        # Look for employment periods and calculate
        # Patterns like "2020-2023", "Jan 2020 - Dec 2023", etc.
        date_patterns = [
            r'(\d{4})\s*[-–—]\s*(\d{4})',  # 2020-2023
            r'(\d{4})\s*[-–—]\s*(?:present|now|current)',  # 2020-present
            r'(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\s*(\d{4})\s*[-–—]\s*(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\s*(\d{4})',  # Jan 2020 - Dec 2023
        ]

        total_years = 0.0
        current_year = datetime.now().year

        for pattern in date_patterns:
            matches = re.findall(pattern, experience_text, re.IGNORECASE)
            for match in matches:
                if len(match) == 2:
                    start_year = int(match[0])
                    end_year = int(match[1]) if match[1].isdigit() else current_year
                else:
                    start_year = int(match[0])
                    end_year = current_year

                if 1990 <= start_year <= current_year and start_year <= end_year:
                    years_diff = end_year - start_year
                    total_years += years_diff
                    print(f"  Found date range: {start_year}-{end_year} = {years_diff} years")

        # If we found date ranges, use the total
        if total_years > 0:
            return total_years

        # Fallback: look for any numbers that might indicate experience
        numbers = re.findall(r'\b(\d+)\b', experience_text)
        if numbers:
            # Take the first reasonable number (between 0 and 50)
            for num_str in numbers:
                num = int(num_str)
                if 0 <= num <= 50:
                    print(f"  Using fallback number: {num} years")
                    return float(num)

        print(f"  No experience found in: {experience_text[:100]}...")
        return 0.0

    def process_names(self, df: pd.DataFrame) -> pd.DataFrame:
        """Standardize and clean names."""
        def clean_name(name):
            if pd.isna(name) or not name.strip():
                return name  # Return original if empty

            name = str(name)
            name = re.sub(r'\s+', ' ', name.strip())
            name = re.sub(r'\b(cv|resume|curriculum|vitae|contact)\b', '', name, flags=re.IGNORECASE)
            
            name_parts = [part.title() for part in name.split() if len(part) > 1 and part.isalpha()]
            return ' '.join(name_parts) if name_parts else name  # Return original if no valid parts

        if 'name_cleaned' not in df.columns and 'name' in df.columns:
            df['name_cleaned'] = df['name'].apply(clean_name)
        return df

    def fetch_job_requirements(self, job_offer_id: str) -> Dict[str, Any]:
        """Fetch job requirements from PostgreSQL database."""
        if not self.db_config:
            print(f"No database configuration provided for job offer {job_offer_id}")
            return None

        # Query for main job offer info
        main_query = sql.SQL("""
            SELECT years_of_experience_required
            FROM job_offer
            WHERE id = %s
        """)

        # Query for skills
        skills_query = sql.SQL("""
            SELECT skill
            FROM job_offer_skills
            WHERE job_offer_id = %s
        """)

        try:
            conn = psycopg2.connect(**self.db_config)
            cursor = conn.cursor()
            
            # Get main job offer data
            cursor.execute(main_query, (job_offer_id,))
            main_result = cursor.fetchone()
            
            if not main_result:
                print(f"No job offer found with ID: {job_offer_id}")
                cursor.close()
                conn.close()
                return None
            
            # Get skills list
            cursor.execute(skills_query, (job_offer_id,))
            skills_results = cursor.fetchall()
            skills = [skill[0].strip().lower() for skill in skills_results] if skills_results else []
            
            cursor.close()
            conn.close()

            requirements = {
                'min_experience': main_result[0],
                'required_skills': skills
            }
            
            print(f"Job offer {job_offer_id} requirements: min_exp={requirements['min_experience']}, skills={len(skills)}")
            return requirements
            
        except Exception as e:
            print(f"Error fetching job requirements for {job_offer_id}: {e}")
            return None

    def hard_filter_cvs(self, processed_df: pd.DataFrame, apply_strict_filters: bool = False) -> pd.DataFrame:
        """
        Filter CVs based on job requirements from PostgreSQL.
        
        Args:
            processed_df: DataFrame with processed CV data
            apply_strict_filters: If True, apply strict filtering. If False, use lenient filtering.
        """
        print(f"Starting hard filtering on {len(processed_df)} CVs...")
        print(f"Strict filtering: {apply_strict_filters}")
        filtered_rows = []
        
        for idx, cv_row in processed_df.iterrows():
            job_offer_id = cv_row['job_offer_id']
            print(f"Processing CV {idx+1}/{len(processed_df)} for job offer {job_offer_id}")
            
            requirements = self.fetch_job_requirements(job_offer_id)
            
            if not requirements:
                print(f"  No requirements found - including CV by default")
                filtered_rows.append(cv_row)
                continue
            
            # Check experience requirement
            cv_experience = cv_row.get('total_experience_years', 0)
            min_exp_required = requirements.get('min_experience')
            
            experience_passes = True
            if min_exp_required is not None and min_exp_required > 0:
                if apply_strict_filters:
                    # Strict: must meet exact requirement
                    if cv_experience < min_exp_required:
                        print(f"  Experience filter FAILED: CV has {cv_experience} years, need {min_exp_required}")
                        experience_passes = False
                    else:
                        print(f"  Experience filter PASSED: CV has {cv_experience} years, need {min_exp_required}")
                else:
                    # Lenient: allow 1-2 years less than required, or any CV with some experience
                    tolerance = max(1, min_exp_required * 0.3)  # 30% tolerance or minimum 1 year
                    if cv_experience < (min_exp_required - tolerance):
                        print(f"  Experience filter FAILED: CV has {cv_experience} years, need {min_exp_required} (tolerance: {tolerance})")
                        experience_passes = False
                    else:
                        print(f"  Experience filter PASSED: CV has {cv_experience} years, need {min_exp_required} (tolerance: {tolerance})")
            else:
                print(f"  No experience requirement")
            
            # Check skills requirement
            required_skills = requirements.get('required_skills', [])
            skills_passes = True
            
            if required_skills:
                cv_skills_text = str(cv_row.get('skills_final', ''))
                if not cv_skills_text or cv_skills_text == 'nan':
                    cv_skills_text = str(cv_row.get('skills', ''))
                
                cv_skills = {s.strip().lower() for s in cv_skills_text.split(',') if s.strip()}
                
                # Use fuzzy matching for skills
                matched_skills = []
                for required_skill in required_skills:
                    for cv_skill in cv_skills:
                        if fuzz.partial_ratio(required_skill.lower(), cv_skill.lower()) > 70:  # Lowered threshold
                            matched_skills.append(required_skill)
                            break
                
                if apply_strict_filters:
                    # Strict: must have all required skills
                    required_match_count = len(required_skills)
                else:
                    # Lenient: must have at least 50% of required skills, minimum 1
                    required_match_count = max(1, len(required_skills) // 2)
                
                if len(matched_skills) < required_match_count:
                    print(f"  Skills filter FAILED: Found {len(matched_skills)}/{len(required_skills)} required skills (need {required_match_count})")
                    print(f"    Required: {required_skills}")
                    print(f"    CV has: {list(cv_skills)}")
                    print(f"    Matched: {matched_skills}")
                    skills_passes = False
                else:
                    print(f"  Skills filter PASSED: Found {len(matched_skills)}/{len(required_skills)} required skills (need {required_match_count})")
            else:
                print(f"  No skills requirement")
            
            # Include CV if it passes all filters
            if experience_passes and skills_passes:
                print(f"  CV ACCEPTED")
                filtered_rows.append(cv_row)
            else:
                print(f"  CV REJECTED")
        
        result_df = pd.DataFrame(filtered_rows)
        print(f"Hard filtering complete: {len(result_df)}/{len(processed_df)} CVs passed")
        return result_df

    def process_dataframe(self, df: pd.DataFrame) -> pd.DataFrame:
        """Process the entire dataframe."""
        print("Starting data processing...")
        processed_df = df.copy()

        # Process names if column exists and not already processed
        if 'name' in processed_df.columns and 'name_cleaned' not in processed_df.columns:
            print("Processing names...")
            processed_df = self.process_names(processed_df)

        # Standardize skills if column exists and not already processed
        if 'skills' in processed_df.columns and 'skills_standardized' not in processed_df.columns:
            print("Standardizing skills...")
            processed_df['skills_standardized'] = processed_df['skills'].apply(self.standardize_skills)

        # Remove duplicate skills if not already done
        if 'skills_standardized' in processed_df.columns and 'skills_final' not in processed_df.columns:
            print("Removing duplicate skills...")
            processed_df['skills_final'] = processed_df['skills_standardized'].apply(self.remove_duplicate_skills)
        elif 'skills' in processed_df.columns and 'skills_final' not in processed_df.columns:
            print("Processing skills directly to final...")
            processed_df['skills_final'] = processed_df['skills'].apply(self.remove_duplicate_skills)

        # Normalize experience dates if column exists and not already processed
        if 'experience' in processed_df.columns and 'experience_normalized' not in processed_df.columns:
            print("Normalizing experience dates...")
            processed_df['experience_normalized'] = processed_df['experience'].apply(self.normalize_date_ranges)

        # Compute experience years if not already done
        if 'total_experience_years' not in processed_df.columns:
            print("Computing total experience years...")
            if 'experience_normalized' in processed_df.columns:
                processed_df['total_experience_years'] = processed_df['experience_normalized'].apply(
                    self.compute_total_experience_years
                )
            elif 'experience' in processed_df.columns:
                processed_df['total_experience_years'] = processed_df['experience'].apply(
                    self.compute_total_experience_years
                )
            else:
                processed_df['total_experience_years'] = 0.0

        processed_df['processed_at'] = datetime.now().isoformat()
        print("Data processing completed!")
        return processed_df

    def save_processed_data(self, df: pd.DataFrame, output_file: str):
        """Save the processed data to CSV."""
        try:
            # Add timestamp to filename to avoid permission issues
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            base_name = output_file.split('.')[0]
            timestamped_file = f"{base_name}_{timestamp}.csv"
            
            df.to_csv(timestamped_file, index=False, encoding='utf-8')
            print(f"Processed data saved to {timestamped_file}")
        except PermissionError:
            # Try alternative filename if original is locked
            alt_file = f"shortlisted_cvs_backup_{timestamp}.csv"
            try:
                df.to_csv(alt_file, index=False, encoding='utf-8')
                print(f"Original file locked, saved to {alt_file}")
            except Exception as e2:
                print(f"Error saving to backup file: {e2}")
                raise
        except Exception as e:
            print(f"Error saving data: {e}")
            raise

    def generate_report(self, original_df: pd.DataFrame, processed_df: pd.DataFrame):
        """Generate a processing report."""
        print("\n" + "="*50)
        print("CV DATA PROCESSING REPORT")
        print("="*50)

        print(f"Original records: {len(original_df)}")
        print(f"Final records: {len(processed_df)}")
        print(f"Filtered out: {len(original_df) - len(processed_df)}")
        
        if len(processed_df) == 0:
            print("No records in final dataset - check filtering criteria!")
            return
        
        # Name processing stats
        name_col = None
        if 'name_cleaned' in processed_df.columns:
            name_col = 'name_cleaned'
        elif 'name' in processed_df.columns:
            name_col = 'name'
            
        if name_col:
            name_count = len(processed_df[processed_df[name_col].notna() & (processed_df[name_col].str.len() > 0)])
            print(f"Records with names: {name_count}")
        else:
            print("No name data available")
                
        # Skills processing stats
        skills_col = None
        if 'skills_final' in processed_df.columns:
            skills_col = 'skills_final'
        elif 'skills' in processed_df.columns:
            skills_col = 'skills'
            
        if skills_col:
            skills_count = len(processed_df[processed_df[skills_col].notna() & (processed_df[skills_col].str.len() > 0)])
            print(f"Records with skills: {skills_count}")
        else:
            print("No skills data available")
                
        # Experience stats
        if 'total_experience_years' in processed_df.columns:
            exp_count = len(processed_df[processed_df['total_experience_years'] > 0])
            print(f"Records with experience years calculated: {exp_count}")
        else:
            print("No experience data available")

        # Skills analysis
        if skills_col:
            all_skills = []
            for skills in processed_df[skills_col].dropna():
                if skills and str(skills) != 'nan':
                    all_skills.extend([s.strip() for s in str(skills).split(',')])

            if all_skills:
                skill_counts = Counter(all_skills)
                print("\nTop 10 most common skills:")
                for skill, count in skill_counts.most_common(10):
                    print(f"  {skill}: {count}")
            else:
                print("\nNo skills data available for analysis")
        else:
            print("\nNo skills column available for analysis")

        # Experience analysis
        if 'total_experience_years' in processed_df.columns:
            exp_data = processed_df['total_experience_years']
            exp_stats = exp_data.describe()
            print("\nExperience statistics:")
            print(f"  Average experience: {exp_stats['mean']:.1f} years")
            print(f"  Max experience: {exp_stats['max']:.1f} years")
            print(f"  Min experience: {exp_stats['min']:.1f} years")
            print(f"  Records with 0 experience: {len(exp_data[exp_data == 0])}")
        else:
            print("\nNo experience data available for statistics")

def main():
    # PostgreSQL configuration
    db_config = {
        'host': 'localhost',
        'database': 'cv_filter',
        'user': 'postgres',
        'password': '2003',
        'port': '5432'
    }

    processor = CVDataProcessor(db_config)
    input_file = "cv_processed_data.csv"
    output_file = "shortlisted_cvs.csv"

    try:
        # Load data
        df = processor.load_data(input_file)
        print(f"Loaded columns: {list(df.columns)}")
        
        # Process dataframe
        processed_df = processor.process_dataframe(df)
        print(f"After processing: {len(processed_df)} records")

        # Try lenient filtering first
        print("\n" + "="*50)
        print("TRYING LENIENT FILTERING FIRST")
        print("="*50)
        shortlisted_df = processor.hard_filter_cvs(processed_df, apply_strict_filters=False)
        print(f"After lenient filtering: {len(shortlisted_df)} records")

        # If no results with lenient filtering, show some sample data for debugging
        if len(shortlisted_df) == 0:
            print("\n" + "="*50)
            print("NO CVS PASSED LENIENT FILTERING - DEBUGGING INFO")
            print("="*50)
            
            # Show sample experience data
            print("\nSample experience data:")
            for idx, row in processed_df.head(5).iterrows():
                print(f"CV {idx+1}:")
                print(f"  Original experience: {str(row.get('experience', 'N/A'))[:100]}...")
                print(f"  Calculated years: {row.get('total_experience_years', 0)}")
                print(f"  Skills: {str(row.get('skills_final', row.get('skills', 'N/A')))[:100]}...")
                print()
            
            # Try without any filtering for debugging
            print("Saving all processed CVs for debugging...")
            processor.save_processed_data(processed_df, "debug_all_cvs.csv")
        else:
            # Save results
            processor.save_processed_data(shortlisted_df, output_file)

        processor.generate_report(df, shortlisted_df if len(shortlisted_df) > 0 else processed_df)

        print(f"\nProcessing complete!")

    except Exception as e:
        print(f"Error in main processing: {e}")
        import traceback
        traceback.print_exc()
        raise


if __name__ == "__main__":
    main()