import os
import sys
import json
import csv
from pathlib import Path
from datetime import datetime
import google.generativeai as genai
import PyPDF2
from docx import Document
import mimetypes
from typing import Dict, List, Tuple, Optional
import logging
import time

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

class CVOrganizationScorer:
    def __init__(self, gemini_api_key: str, data_folder_path: str = "data"):
        """
        Initialize the CV Organization Scorer
        
        Args:
            gemini_api_key: Your Google Gemini API key
            data_folder_path: Path to the folder containing CV files
        """
        self.data_folder_path = Path(data_folder_path)
        self.results = []
        self.shortlist = []
        
        # Configure Gemini AI
        genai.configure(api_key=gemini_api_key)
        
        # Try different model names in order of preference
        model_names = ['gemini-1.5-flash', 'gemini-1.5-pro', 'gemini-pro']
        self.model = None
        
        for model_name in model_names:
            try:
                self.model = genai.GenerativeModel(model_name)
                logger.info(f"Successfully initialized Gemini model: {model_name}")
                break
            except Exception as e:
                logger.warning(f"Failed to initialize model {model_name}: {e}")
                continue
        
        if self.model is None:
            logger.error("Failed to initialize any Gemini model. Checking available models...")
            self._list_available_models()
            raise Exception("Could not initialize Gemini AI model")
        
        # Supported file types
        self.supported_extensions = {'.pdf', '.docx', '.txt'}
    
    def _list_available_models(self):
        """List available Gemini models"""
        try:
            models = genai.list_models()
            logger.info("Available Gemini models:")
            for model in models:
                if 'generateContent' in model.supported_generation_methods:
                    logger.info(f"  - {model.name}")
        except Exception as e:
            logger.error(f"Error listing models: {e}")
        
    def extract_text_from_file(self, file_path: Path) -> Optional[str]:
        """
        Extract text content from different file types
        
        Args:
            file_path: Path to the CV file
            
        Returns:
            Extracted text content or None if extraction fails
        """
        try:
            file_extension = file_path.suffix.lower()
            
            if file_extension == '.pdf':
                return self._extract_from_pdf(file_path)
            elif file_extension == '.docx':
                return self._extract_from_docx(file_path)
            elif file_extension == '.txt':
                return self._extract_from_txt(file_path)
            else:
                logger.warning(f"Unsupported file format: {file_extension}")
                return None
                
        except Exception as e:
            logger.error(f"Error extracting text from {file_path}: {str(e)}")
            return None
    
    def _extract_from_pdf(self, file_path: Path) -> str:
        """Extract text from PDF file"""
        text = ""
        with open(file_path, 'rb') as file:
            pdf_reader = PyPDF2.PdfReader(file)
            for page in pdf_reader.pages:
                text += page.extract_text() + "\n"
        return text.strip()
    
    def _extract_from_docx(self, file_path: Path) -> str:
        """Extract text from DOCX file"""
        doc = Document(file_path)
        text = ""
        for paragraph in doc.paragraphs:
            text += paragraph.text + "\n"
        return text.strip()
    
    def _extract_from_txt(self, file_path: Path) -> str:
        """Extract text from TXT file"""
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
            return file.read().strip()
    
    def analyze_cv_organization_with_gemini(self, cv_text: str, file_name: str) -> Dict:
        """
        Use Gemini AI to analyze CV organization and provide scoring
        
        Args:
            cv_text: The extracted CV text
            file_name: Name of the CV file
            
        Returns:
            Dictionary containing organization analysis and scores
        """
        try:
            # Create a comprehensive prompt for CV organization analysis
            prompt = f"""
            Please analyze the following CV for organization and structure quality. 
            Provide a detailed assessment with scores (0-100) for each category:

            CV File: {file_name}
            
            CV Content:
            {cv_text[:4000]}  # Limit text to avoid token limits
            
            Please evaluate and score (0-100) the following aspects:
            
            1. OVERALL_STRUCTURE: How well-organized is the overall layout and flow?
            2. CONTACT_INFO: Is contact information clearly presented and complete?
            3. SECTIONS_CLARITY: Are different sections (education, experience, skills) clearly defined?
            4. CHRONOLOGICAL_ORDER: Is information presented in logical chronological order?
            5. FORMATTING_CONSISTENCY: Is formatting consistent throughout?
            6. READABILITY: How easy is it to scan and read quickly?
            7. PROFESSIONAL_APPEARANCE: Does it look professional and polished?
            8. COMPLETENESS: Are all important sections present?
            
            Respond in the following JSON format:
            {{
                "overall_organization_score": <0-100>,
                "detailed_scores": {{
                    "overall_structure": <0-100>,
                    "contact_info": <0-100>,
                    "sections_clarity": <0-100>,
                    "chronological_order": <0-100>,
                    "formatting_consistency": <0-100>,
                    "readability": <0-100>,
                    "professional_appearance": <0-100>,
                    "completeness": <0-100>
                }},
                "strengths": ["list of strengths"],
                "weaknesses": ["list of weaknesses"],
                "improvement_suggestions": ["list of specific suggestions"],
                "organization_level": "EXCELLENT/GOOD/FAIR/POOR"
            }}
            """
            
            # Get response from Gemini
            response = self.model.generate_content(prompt)
            
            # Parse the JSON response
            try:
                # Extract JSON from the response text
                response_text = response.text
                # Find JSON content (sometimes Gemini adds extra text)
                start_idx = response_text.find('{')
                end_idx = response_text.rfind('}') + 1
                
                if start_idx >= 0 and end_idx > start_idx:
                    json_text = response_text[start_idx:end_idx]
                    analysis = json.loads(json_text)
                else:
                    raise ValueError("No valid JSON found in response")
                    
            except (json.JSONDecodeError, ValueError) as e:
                logger.error(f"Error parsing Gemini response: {e}")
                # Fallback analysis
                analysis = self._create_fallback_analysis(cv_text, file_name)
            
            return analysis
            
        except Exception as e:
            logger.error(f"Error in Gemini analysis for {file_name}: {str(e)}")
            return self._create_fallback_analysis(cv_text, file_name)
    
    def _create_fallback_analysis(self, cv_text: str, file_name: str) -> Dict:
        """Create a basic fallback analysis when Gemini fails"""
        # Simple heuristic-based scoring
        text_length = len(cv_text)
        has_email = '@' in cv_text
        has_phone = any(char.isdigit() for char in cv_text)
        section_keywords = ['experience', 'education', 'skills', 'work', 'employment']
        sections_found = sum(1 for keyword in section_keywords if keyword.lower() in cv_text.lower())
        
        basic_score = min(100, max(20, 
            (text_length // 50) + 
            (20 if has_email else 0) + 
            (20 if has_phone else 0) + 
            (sections_found * 10)
        ))
        
        return {
            "overall_organization_score": basic_score,
            "detailed_scores": {
                "overall_structure": basic_score,
                "contact_info": 60 if (has_email and has_phone) else 30,
                "sections_clarity": min(80, sections_found * 20),
                "chronological_order": 50,
                "formatting_consistency": 50,
                "readability": basic_score,
                "professional_appearance": 50,
                "completeness": min(80, sections_found * 15)
            },
            "strengths": ["Basic CV structure detected"],
            "weaknesses": ["Detailed analysis unavailable - using fallback scoring"],
            "improvement_suggestions": ["Consider professional CV formatting", "Ensure all sections are clearly defined"],
            "organization_level": "FAIR" if basic_score >= 60 else "POOR"
        }
    
    def scan_cv_files(self) -> List[Path]:
        """
        Scan the data folder for CV files
        
        Returns:
            List of CV file paths
        """
        cv_files = []
        
        if not self.data_folder_path.exists():
            logger.error(f"Data folder does not exist: {self.data_folder_path}")
            return cv_files
        
        # Scan through job directories (following your Java code structure)
        for job_dir in self.data_folder_path.iterdir():
            if job_dir.is_dir():
                logger.info(f"Scanning job directory: {job_dir.name}")
                
                for file_path in job_dir.iterdir():
                    if file_path.is_file() and file_path.suffix.lower() in self.supported_extensions:
                        cv_files.append(file_path)
                        logger.info(f"Found CV file: {file_path.name}")
        
        return cv_files
    
    def process_all_cvs(self) -> None:
        """
        Process all CV files and generate organization scores
        """
        logger.info("Starting CV organization analysis...")
        
        cv_files = self.scan_cv_files()
        
        if not cv_files:
            logger.warning("No CV files found to process")
            return
        
        logger.info(f"Found {len(cv_files)} CV files to analyze")
        
        total_files = len(cv_files)
        processed_count = 0
        
        for cv_file in cv_files:
            try:
                logger.info(f"Processing ({processed_count + 1}/{total_files}): {cv_file.name}")
                
                # Extract text from the CV file
                cv_text = self.extract_text_from_file(cv_file)
                
                if not cv_text:
                    logger.warning(f"Could not extract text from {cv_file.name}")
                    continue
                
                # Analyze organization with Gemini
                analysis = self.analyze_cv_organization_with_gemini(cv_text, cv_file.name)
                
                # Extract IDs from file path (following your Java logic)
                job_id = self._extract_job_id_from_path(cv_file)
                user_id = self._extract_user_id_from_filename(cv_file.name)
                company_id = self._extract_company_id_from_filename(cv_file.name)
                
                # Store results
                result = {
                    'file_path': str(cv_file),
                    'file_name': cv_file.name,
                    'job_id': job_id,
                    'user_id': user_id,
                    'company_id': company_id,
                    'file_size_kb': cv_file.stat().st_size / 1024,
                    'analysis_timestamp': datetime.now().isoformat(),
                    **analysis
                }
                
                self.results.append(result)
                
                # Add to shortlist if score is above threshold (e.g., 80)
                if analysis['overall_organization_score'] >= 80:
                    self.shortlist.append(result)
                    logger.info(f"Added to shortlist: {cv_file.name}")
                
                processed_count += 1
                
                logger.info(f"✓ Analyzed {cv_file.name} - Score: {analysis['overall_organization_score']}/100")
                
                # Add small delay to avoid API rate limits
                time.sleep(1)
                
            except Exception as e:
                logger.error(f"Error processing {cv_file.name}: {str(e)}")
                continue
        
        logger.info(f"Completed analysis of {processed_count} CV files")
        logger.info(f"Shortlist contains {len(self.shortlist)} well-organized CVs")
    
    def _extract_job_id_from_path(self, file_path: Path) -> Optional[str]:
        """Extract job ID from directory name"""
        try:
            parent_dir = file_path.parent.name
            # Try different patterns similar to your Java code
            import re
            patterns = [
                r'job[_-]?(\d+)',
                r'^(\d+)$',
                r'(\d+)'
            ]
            
            for pattern in patterns:
                match = re.search(pattern, parent_dir, re.IGNORECASE)
                if match:
                    return match.group(1)
            
            return None
        except:
            return None
    
    def _extract_user_id_from_filename(self, filename: str) -> Optional[str]:
        """Extract user ID from filename"""
        try:
            import re
            patterns = [
                r'_user_(\d+)_',
                r'user(\d+)',
                r'_(\d+)_',
                r'(\d+)'
            ]
            
            for pattern in patterns:
                match = re.search(pattern, filename)
                if match:
                    return match.group(1)
            
            return None
        except:
            return None
    
    def _extract_company_id_from_filename(self, filename: str) -> Optional[str]:
        """Extract company ID from filename"""
        try:
            import re
            patterns = [
                r'_company_(\d+)_',
                r'_comp(\d+)_',
                r'company(\d+)',
                r'comp(\d+)',
                r'_co_(\d+)_'
            ]
            
            for pattern in patterns:
                match = re.search(pattern, filename, re.IGNORECASE)
                if match:
                    return match.group(1)
            
            return None
        except:
            return None
    
    def save_results_to_csv(self, output_file: str = "cv_organization_scores.csv") -> None:
        """
        Save analysis results to CSV file
        
        Args:
            output_file: Name of the output CSV file
        """
        if not self.results:
            logger.warning("No results to save")
            return
        
        try:
            with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
                fieldnames = [
                    'file_name', 'file_path', 'job_id', 'user_id', 'company_id',
                    'file_size_kb', 'overall_organization_score', 'organization_level',
                    'overall_structure', 'contact_info', 'sections_clarity',
                    'chronological_order', 'formatting_consistency', 'readability',
                    'professional_appearance', 'completeness', 'strengths',
                    'weaknesses', 'improvement_suggestions', 'analysis_timestamp'
                ]
                
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                
                for result in self.results:
                    # Flatten the detailed scores
                    row = {
                        'file_name': result['file_name'],
                        'file_path': result['file_path'],
                        'job_id': result.get('job_id', ''),
                        'user_id': result.get('user_id', ''),
                        'company_id': result.get('company_id', ''),
                        'file_size_kb': round(result['file_size_kb'], 2),
                        'overall_organization_score': result['overall_organization_score'],
                        'organization_level': result['organization_level'],
                        'analysis_timestamp': result['analysis_timestamp']
                    }
                    
                    # Add detailed scores
                    for score_name, score_value in result['detailed_scores'].items():
                        row[score_name] = score_value
                    
                    # Convert lists to strings
                    row['strengths'] = '; '.join(result.get('strengths', []))
                    row['weaknesses'] = '; '.join(result.get('weaknesses', []))
                    row['improvement_suggestions'] = '; '.join(result.get('improvement_suggestions', []))
                    
                    writer.writerow(row)
            
            logger.info(f"Results saved to {output_file}")
            
        except Exception as e:
            logger.error(f"Error saving results to CSV: {str(e)}")
    
    def save_shortlist_to_csv(self, output_file: str = "cv_shortlist.csv") -> None:
        """
        Save shortlist to CSV file
        
        Args:
            output_file: Name of the output CSV file
        """
        if not self.shortlist:
            logger.warning("No CVs in shortlist to save")
            return
        
        try:
            with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
                fieldnames = [
                    'file_name', 'file_path', 'job_id', 'user_id', 'company_id',
                    'overall_organization_score', 'organization_level',
                    'strengths', 'weaknesses', 'improvement_suggestions'
                ]
                
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                
                for result in self.shortlist:
                    row = {
                        'file_name': result['file_name'],
                        'file_path': result['file_path'],
                        'job_id': result.get('job_id', ''),
                        'user_id': result.get('user_id', ''),
                        'company_id': result.get('company_id', ''),
                        'overall_organization_score': result['overall_organization_score'],
                        'organization_level': result['organization_level'],
                        'strengths': '; '.join(result.get('strengths', [])),
                        'weaknesses': '; '.join(result.get('weaknesses', [])),
                        'improvement_suggestions': '; '.join(result.get('improvement_suggestions', []))
                    }
                    
                    writer.writerow(row)
            
            logger.info(f"Shortlist saved to {output_file}")
            
        except Exception as e:
            logger.error(f"Error saving shortlist to CSV: {str(e)}")
    
    def generate_summary_report(self) -> None:
        """Generate a summary report of the analysis"""
        if not self.results:
            logger.warning("No results available for summary")
            return
        
        total_cvs = len(self.results)
        scores = [result['overall_organization_score'] for result in self.results]
        
        avg_score = sum(scores) / len(scores)
        max_score = max(scores)
        min_score = min(scores)
        
        # Count by organization level
        levels = [result['organization_level'] for result in self.results]
        level_counts = {level: levels.count(level) for level in set(levels)}
        
        print("\n" + "="*50)
        print("CV ORGANIZATION ANALYSIS SUMMARY")
        print("="*50)
        print(f"Total CVs Analyzed: {total_cvs}")
        print(f"Average Organization Score: {avg_score:.1f}/100")
        print(f"Highest Score: {max_score}/100")
        print(f"Lowest Score: {min_score}/100")
        print("\nDistribution by Organization Level:")
        for level, count in level_counts.items():
            percentage = (count / total_cvs) * 100
            print(f"  {level}: {count} CVs ({percentage:.1f}%)")
        
        # Shortlist summary
        print(f"\nShortlist Summary (Score ≥ 80): {len(self.shortlist)} CVs")
        if self.shortlist:
            print("\nTop 5 Best Organized CVs:")
            sorted_shortlist = sorted(self.shortlist, key=lambda x: x['overall_organization_score'], reverse=True)
            for i, result in enumerate(sorted_shortlist[:5], 1):
                print(f"  {i}. {result['file_name']} - Score: {result['overall_organization_score']}/100")
        
        # Bottom 5 CVs needing improvement
        print(f"\nTop 5 CVs Needing Organization Improvement:")
        sorted_results = sorted(self.results, key=lambda x: x['overall_organization_score'])
        for i, result in enumerate(sorted_results[:5], 1):
            print(f"  {i}. {result['file_name']} - Score: {result['overall_organization_score']}/100")
        
        print("="*50)

def main():
    """Main function to run the CV organization scorer"""
    
    # Configuration
    GEMINI_API_KEY = "AIzaSyD4Qi1XnUnavw3pGcvfVcU7Ud7_5jH-1t0"  # Replace with your actual API key
    DATA_FOLDER_PATH = "data"  # Path to your CV folder
    OUTPUT_CSV_FILE = "cv_organization_scores.csv"
    SHORTLIST_CSV_FILE = "cv_shortlist.csv"
    
    try:
        # Initialize the scorer
        scorer = CVOrganizationScorer(GEMINI_API_KEY, DATA_FOLDER_PATH)
        
        # Process all CVs
        scorer.process_all_cvs()
        
        # Save results to CSV
        scorer.save_results_to_csv(OUTPUT_CSV_FILE)
        
        # Save shortlist to CSV
        scorer.save_shortlist_to_csv(SHORTLIST_CSV_FILE)
        
        # Generate summary report
        scorer.generate_summary_report()
        
        print(f"\nAnalysis complete!")
        print(f"- Results saved to {OUTPUT_CSV_FILE}")
        print(f"- Shortlist saved to {SHORTLIST_CSV_FILE}")
        
    except Exception as e:
        logger.error(f"Error in main execution: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()