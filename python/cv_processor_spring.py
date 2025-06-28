#!/usr/bin/env python3
"""
Optimized CV Processor for Spring Integration
- Reduced processing time from minutes to seconds
- Simple scoring without heavy AI/NLP
- Direct CSV processing without database calls
"""

import pandas as pd
import argparse
import json
import sys
import time
import re
from datetime import datetime
from typing import Dict, Any
import os

class FastCVProcessor:
    """Fast CV processor optimized for Spring integration"""
    
    def __init__(self):
        self.start_time = time.time()
        
        # Simple skills mapping (no fuzzy matching)
        self.common_skills = {
            'java', 'python', 'javascript', 'js', 'react', 'angular', 'vue',
            'node', 'express', 'spring', 'django', 'flask', 'sql', 'mysql',
            'postgresql', 'mongodb', 'docker', 'kubernetes', 'aws', 'azure',
            'git', 'html', 'css', 'php', 'c++', 'c#', 'golang', 'typescript'
        }
        
        # Education levels
        self.education_levels = {
            'phd': 5, 'doctorate': 5, 'doctor': 5,
            'master': 4, 'mba': 4, 'msc': 4, 'm.sc': 4,
            'bachelor': 3, 'bs': 3, 'b.sc': 3, 'undergraduate': 3,
            'associate': 2, 'diploma': 2, 'certificate': 2,
            'high school': 1, 'secondary': 1
        }

    def process_csv(self, input_file: str, output_file: str):
        """Process CV data from CSV file"""
        try:
            print(f"Processing: {input_file}")
            
            # Read input CSV
            df = pd.read_csv(input_file)
            print(f"Loaded {len(df)} records")
            
            # Process each row
            for idx, row in df.iterrows():
                # Fast skills processing
                df.at[idx, 'skills_standardized'] = self.fast_standardize_skills(row.get('skills', ''))
                
                # Fast experience calculation  
                df.at[idx, 'total_experience_years'] = self.fast_calculate_experience(row.get('experience', ''))
                
                # Fast education level
                df.at[idx, 'education_level'] = self.fast_extract_education(row.get('education', ''))
                
                # Processing timestamp
                df.at[idx, 'processed_at'] = datetime.now().isoformat()
            
            # Save processed data
            df.to_csv(output_file, index=False)
            
            elapsed = time.time() - self.start_time
            print(f"Processing completed in {elapsed:.2f} seconds")
            print(f"Saved to: {output_file}")
            
        except Exception as e:
            print(f"Error: {e}", file=sys.stderr)
            sys.exit(1)

    def fast_standardize_skills(self, skills_text: str) -> str:
        """Fast skills standardization without fuzzy matching"""
        if pd.isna(skills_text) or not str(skills_text).strip():
            return ""
        
        skills_text = str(skills_text).lower()
        # Simple regex split
        skills = re.split(r'[,;•\n\r\t\|]+', skills_text)
        
        standardized = set()
        for skill in skills:
            skill = skill.strip()
            if skill and len(skill) > 1:
                # Remove common prefixes
                skill = re.sub(r'^(compétences|skills|technical)[:.\s]*', '', skill)
                skill = skill.strip()
                
                if skill in self.common_skills:
                    standardized.add(skill.title())
                elif len(skill) > 2:
                    standardized.add(skill.title())
        
        return ', '.join(sorted(standardized))

    def fast_calculate_experience(self, experience_text: str) -> float:
        """Fast experience calculation"""
        if pd.isna(experience_text) or not str(experience_text).strip():
            return 0.0
        
        text = str(experience_text).lower()
        
        # Look for direct year mentions
        patterns = [
            r'(\d+)\s*(?:years?|yrs?|ans?)',
            r'(\d+)\+?\s*(?:years?|yrs?)',
            r'experience[:\s]*(\d+)',
        ]
        
        for pattern in patterns:
            match = re.search(pattern, text)
            if match:
                years = float(match.group(1))
                if 0 <= years <= 50:  # Reasonable range
                    return years
        
        # Look for date ranges
        current_year = datetime.now().year
        date_matches = re.findall(r'(\d{4})\s*[-–—]\s*(\d{4}|present|now|current)', text)
        
        total_years = 0.0
        for match in date_matches:
            start_year = int(match[0])
            end_year = current_year if match[1] in ['present', 'now', 'current'] else int(match[1])
            
            if 1990 <= start_year <= current_year and start_year <= end_year:
                total_years += (end_year - start_year)
        
        return min(total_years, 50.0)  # Cap at 50 years

    def fast_extract_education(self, education_text: str) -> str:
        """Fast education level extraction"""
        if pd.isna(education_text) or not str(education_text).strip():
            return "NONE_SPECIFIED"
        
        text = str(education_text).lower()
        
        # Check for education keywords (ordered by priority)
        for keyword, level in sorted(self.education_levels.items(), key=lambda x: x[1], reverse=True):
            if keyword in text:
                return {5: 'PHD', 4: 'MASTER', 3: 'BACHELOR', 2: 'ASSOCIATE', 1: 'HIGH_SCHOOL'}[level]
        
        return "NONE_SPECIFIED"

def main():
    parser = argparse.ArgumentParser(description='Fast CV Processor for Spring Integration')
    parser.add_argument('--input', required=True, help='Input CSV file path')
    parser.add_argument('--output', required=True, help='Output CSV file path')
    
    args = parser.parse_args()
    
    # Validate input file exists
    if not os.path.exists(args.input):
        print(f"Error: Input file not found: {args.input}", file=sys.stderr)
        sys.exit(1)
    
    processor = FastCVProcessor()
    processor.process_csv(args.input, args.output)

if __name__ == "__main__":
    main()