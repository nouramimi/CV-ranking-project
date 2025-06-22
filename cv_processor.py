import pandas as pd
import numpy as np
import re
from datetime import datetime
from collections import Counter
import difflib
from typing import List, Dict, Set, Tuple

# NLP Libraries
import spacy
from spacy.matcher import Matcher
from fuzzywuzzy import fuzz, process
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem import WordNetLemmatizer

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

    Required NLP Tools:
    - spaCy (with en_core_web_sm model): For entity recognition and text processing
    - fuzzywuzzy: For fuzzy string matching and duplicate detection
    - NLTK: For text preprocessing and lemmatization
    - pandas: For data manipulation
    - numpy: For numerical operations
    - re: For regular expressions
    """

    def __init__(self):
        try:
            self.nlp = spacy.load("en_core_web_sm")
        except OSError:
            print("Please install spaCy English model: python -m spacy download en_core_web_sm")
            raise

        self.lemmatizer = WordNetLemmatizer()
        self.stop_words = set(stopwords.words('english'))

        self.skills_mapping = {
            'js': 'JavaScript',
            'javascript': 'JavaScript',
            'py': 'Python',
            'python': 'Python',
            'java': 'Java',
            'c++': 'C++',
            'cpp': 'C++',
            'c#': 'C#',
            'csharp': 'C#',
            'html': 'HTML',
            'css': 'CSS',
            'php': 'PHP',
            'typescript': 'TypeScript',
            'ts': 'TypeScript',
            'go': 'Go',
            'golang': 'Go',
            'rust': 'Rust',
            'swift': 'Swift',
            'kotlin': 'Kotlin',
            'scala': 'Scala',
            'r': 'R',
            'matlab': 'MATLAB',

            'react': 'React',
            'reactjs': 'React',
            'react.js': 'React',
            'angular': 'Angular',
            'angularjs': 'Angular',
            'vue': 'Vue.js',
            'vuejs': 'Vue.js',
            'vue.js': 'Vue.js',
            'node': 'Node.js',
            'nodejs': 'Node.js',
            'node.js': 'Node.js',
            'express': 'Express.js',
            'expressjs': 'Express.js',
            'express.js': 'Express.js',
            'django': 'Django',
            'flask': 'Flask',
            'spring': 'Spring',
            'springboot': 'Spring Boot',
            'spring boot': 'Spring Boot',
            'laravel': 'Laravel',
            'rails': 'Ruby on Rails',
            'ruby on rails': 'Ruby on Rails',
            'bootstrap': 'Bootstrap',
            'jquery': 'jQuery',
            'redux': 'Redux',

            'sql': 'SQL',
            'mysql': 'MySQL',
            'postgresql': 'PostgreSQL',
            'postgres': 'PostgreSQL',
            'mongodb': 'MongoDB',
            'mongo': 'MongoDB',
            'redis': 'Redis',
            'elasticsearch': 'Elasticsearch',
            'oracle': 'Oracle',
            'sqlite': 'SQLite',
            'cassandra': 'Cassandra',
            'neo4j': 'Neo4j',

            'git': 'Git',
            'github': 'GitHub',
            'gitlab': 'GitLab',
            'docker': 'Docker',
            'kubernetes': 'Kubernetes',
            'k8s': 'Kubernetes',
            'jenkins': 'Jenkins',
            'aws': 'AWS',
            'amazon web services': 'AWS',
            'azure': 'Microsoft Azure',
            'gcp': 'Google Cloud Platform',
            'google cloud': 'Google Cloud Platform',
            'terraform': 'Terraform',
            'ansible': 'Ansible',
            'maven': 'Maven',
            'gradle': 'Gradle',
            'npm': 'NPM',
            'webpack': 'Webpack',
            'babel': 'Babel',
            'eslint': 'ESLint',
            'jest': 'Jest',
            'junit': 'JUnit',
            'selenium': 'Selenium',
            'postman': 'Postman',
            'jira': 'Jira',
            'confluence': 'Confluence',
            'slack': 'Slack',
            'trello': 'Trello',

            'agile': 'Agile',
            'scrum': 'Scrum',
            'kanban': 'Kanban',
            'devops': 'DevOps',
            'ci/cd': 'CI/CD',
            'tdd': 'Test-Driven Development',
            'bdd': 'Behavior-Driven Development',

            'machine learning': 'Machine Learning',
            'ml': 'Machine Learning',
            'artificial intelligence': 'Artificial Intelligence',
            'ai': 'Artificial Intelligence',
            'deep learning': 'Deep Learning',
            'neural networks': 'Neural Networks',
            'tensorflow': 'TensorFlow',
            'pytorch': 'PyTorch',
            'scikit-learn': 'Scikit-learn',
            'pandas': 'Pandas',
            'numpy': 'NumPy',
            'matplotlib': 'Matplotlib',
            'seaborn': 'Seaborn',
            'jupyter': 'Jupyter',
            'data science': 'Data Science',
            'data analysis': 'Data Analysis',
            'data visualization': 'Data Visualization',
            'statistics': 'Statistics',
            'big data': 'Big Data',
            'hadoop': 'Hadoop',
            'spark': 'Apache Spark',
            'kafka': 'Apache Kafka',
        }

        self.education_levels = {
            'phd': 5, 'doctorate': 5, 'doctorat': 5, 'doctoral': 5,
            'master': 4, 'masters': 4, 'msc': 4, 'ma': 4, 'mba': 4,
            'bachelor': 3, 'bachelors': 3, 'bsc': 3, 'ba': 3, 'licence': 3,
            'associate': 2, 'diploma': 2, 'bts': 2, 'dut': 2,
            'certificate': 1, 'certificat': 1, 'high school': 1, 'baccalauréat': 1
        }

    def load_data(self, csv_file_path: str) -> pd.DataFrame:
        """Load the CV data from CSV file."""
        try:
            df = pd.read_csv(csv_file_path)
            print(f"Loaded {len(df)} records from {csv_file_path}")
            return df
        except Exception as e:
            print(f"Error loading data: {e}")
            raise

    def standardize_skills(self, skills_text: str) -> str:
        """Standardize skills using the mapping dictionary and fuzzy matching."""
        if pd.isna(skills_text) or not skills_text.strip():
            return ""

        skills_text = skills_text.lower()

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
        if pd.isna(skills_text) or not skills_text.strip():
            return ""

        skills = [s.strip() for s in skills_text.split(',') if s.strip()]
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
        if pd.isna(text) or not text.strip():
            return ""

        patterns = [
            (r'(\d{2})/(\d{4})\s*[-–—]\s*(\d{2})/(\d{4})', r'\2-\1 to \4-\3'),
            (r'(\d{4})/(\d{2})\s*[-–—]\s*(\d{4})/(\d{2})', r'\1-\2 to \3-\4'),
            (r'(\d{4})\s*[-–—]\s*(\d{4})', r'\1 to \2'),
            (r'(\d{2})/(\d{4})\s*[-–—]\s*(present|now|current|actuel|aujourd\'hui)', r'\2-\1 to Present'),
            (r'(\d{4})\s*[-–—]\s*(present|now|current|actuel|aujourd\'hui)', r'\1 to Present'),
        ]

        normalized_text = text
        for pattern, replacement in patterns:
            normalized_text = re.sub(pattern, replacement, normalized_text, flags=re.IGNORECASE)

        return normalized_text

    def extract_salary_expectations(self, text: str) -> str:
        """Extract and normalize salary expectations."""
        if pd.isna(text) or not text.strip():
            return ""

        salary_patterns = [
            r'(\d+k?)\s*[-–]\s*(\d+k?)\s*(€|eur|euro|dollars?|\$|usd)',
            r'(\d+k?)\s*(€|eur|euro|dollars?|\$|usd)',
            r'salary\s*expectation:?\s*(\d+k?)\s*(€|eur|euro|dollars?|\$|usd)',
            r'expected\s*salary:?\s*(\d+k?)\s*(€|eur|euro|dollars?|\$|usd)',
        ]

        for pattern in salary_patterns:
            match = re.search(pattern, text, re.IGNORECASE)
            if match:
                return match.group(0)

        return ""

    def compute_total_experience_years(self, experience_text: str) -> float:
        """Compute total years of experience from experience text."""
        if pd.isna(experience_text) or not experience_text.strip():
            return 0.0

        direct_patterns = [
            r'(\d+)\s*(?:years?|ans?|yrs?)\s*(?:of\s*)?(?:experience|expérience)',
            r'(\d+)\+?\s*(?:years?|ans?|yrs?)',
        ]

        for pattern in direct_patterns:
            match = re.search(pattern, experience_text, re.IGNORECASE)
            if match:
                return float(match.group(1))

        date_ranges = re.findall(r'(\d{4})\s*[-–—]\s*(?:(\d{4})|(?:present|now|current))', experience_text, re.IGNORECASE)

        total_years = 0.0
        current_year = datetime.now().year

        for start_year, end_year in date_ranges:
            start = int(start_year)
            end = int(end_year) if end_year else current_year

            if start <= end <= current_year and start >= 1990:  # Sanity check
                total_years += (end - start)

        return total_years

    def compute_highest_degree_level(self, education_text: str) -> int:
        """Compute the highest degree level from education text."""
        if pd.isna(education_text) or not education_text.strip():
            return 0

        education_lower = education_text.lower()
        highest_level = 0

        for degree, level in self.education_levels.items():
            if degree in education_lower:
                highest_level = max(highest_level, level)

        return highest_level

    def process_names(self, df: pd.DataFrame) -> pd.DataFrame:
        """Standardize and clean names."""
        def clean_name(name):
            if pd.isna(name) or not name.strip():
                return ""

            name = re.sub(r'\s+', ' ', name.strip())

            name = re.sub(r'\b(cv|resume|curriculum|vitae|contact|email|phone|tel|mobile)\b', '', name, flags=re.IGNORECASE)

            name_parts = name.split()
            cleaned_parts = []

            for part in name_parts:
                if len(part) > 1 and part.isalpha():
                    cleaned_parts.append(part.title())

            return ' '.join(cleaned_parts) if cleaned_parts else ""

        df['name_cleaned'] = df['name'].apply(clean_name)
        return df

    def process_dataframe(self, df: pd.DataFrame) -> pd.DataFrame:
        print("Starting data processing...")

        processed_df = df.copy()

        print("Processing names...")
        processed_df = self.process_names(processed_df)

        print("Standardizing skills...")
        processed_df['skills_standardized'] = processed_df['skills'].apply(self.standardize_skills)

        print("Removing duplicate skills...")
        processed_df['skills_final'] = processed_df['skills_standardized'].apply(self.remove_duplicate_skills)

        print("Normalizing experience dates...")
        processed_df['experience_normalized'] = processed_df['experience'].apply(self.normalize_date_ranges)

        print("Normalizing education dates...")
        processed_df['education_normalized'] = processed_df['education'].apply(self.normalize_date_ranges)

        print("Extracting salary expectations...")
        processed_df['salary_expectations'] = processed_df['description'].apply(self.extract_salary_expectations)

        print("Computing total experience years...")
        processed_df['total_experience_years'] = processed_df['experience_normalized'].apply(self.compute_total_experience_years)

        print("Computing highest degree level...")
        processed_df['highest_degree_level'] = processed_df['education_normalized'].apply(self.compute_highest_degree_level)

        processed_df['processed_at'] = datetime.now().isoformat()

        print("Data processing completed!")
        return processed_df

    def save_processed_data(self, df: pd.DataFrame, output_file: str):
        """Save the processed data to CSV."""
        try:
            df.to_csv(output_file, index=False, encoding='utf-8')
            print(f"Processed data saved to {output_file}")
        except Exception as e:
            print(f"Error saving data: {e}")
            raise

    def generate_report(self, original_df: pd.DataFrame, processed_df: pd.DataFrame):
        """Generate a processing report."""
        print("\n" + "="*50)
        print("CV DATA PROCESSING REPORT")
        print("="*50)

        print(f"Total records processed: {len(processed_df)}")
        print(f"Records with names: {len(processed_df[processed_df['name_cleaned'].str.len() > 0])}")
        print(f"Records with standardized skills: {len(processed_df[processed_df['skills_final'].str.len() > 0])}")
        print(f"Records with experience years calculated: {len(processed_df[processed_df['total_experience_years'] > 0])}")
        print(f"Records with degree level identified: {len(processed_df[processed_df['highest_degree_level'] > 0])}")

        all_skills = []
        for skills in processed_df['skills_final'].dropna():
            if skills:
                all_skills.extend([s.strip() for s in skills.split(',')])

        skill_counts = Counter(all_skills)
        print(f"\nTop 10 most common skills:")
        for skill, count in skill_counts.most_common(10):
            print(f"  {skill}: {count}")

        exp_stats = processed_df['total_experience_years'].describe()
        print(f"\nExperience statistics:")
        print(f"  Average experience: {exp_stats['mean']:.1f} years")
        print(f"  Max experience: {exp_stats['max']:.1f} years")
        print(f"  Min experience: {exp_stats['min']:.1f} years")

        degree_counts = processed_df['highest_degree_level'].value_counts().sort_index()
        degree_names = {0: 'Unknown', 1: 'Certificate/High School', 2: 'Associate/Diploma',
                       3: 'Bachelor', 4: 'Master', 5: 'PhD/Doctorate'}

        print(f"\nDegree level distribution:")
        for level, count in degree_counts.items():
            print(f"  {degree_names.get(level, f'Level {level}')}: {count}")


def main():

    processor = CVDataProcessor()

    input_file = "cv_extracted_info.csv"
    output_file = "cv_processed_data.csv"

    try:
        df = processor.load_data(input_file)

        processed_df = processor.process_dataframe(df)

        processor.save_processed_data(processed_df, output_file)

        processor.generate_report(df, processed_df)

        print(f"\nProcessing complete! Check {output_file} for results.")

    except Exception as e:
        print(f"Error in main processing: {e}")
        raise


if __name__ == "__main__":
    main()