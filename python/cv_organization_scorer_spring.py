#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Fixed CV Organization Scorer - Windows Encoding Compatible
"""

import pandas as pd
import argparse
import json
import sys
import time
import re
import os
from datetime import datetime
from typing import Dict, Any

# Fix Windows encoding issues
import locale
import codecs

# Set UTF-8 encoding for Windows
if sys.platform.startswith('win'):
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout.detach())
    sys.stderr = codecs.getwriter('utf-8')(sys.stderr.detach())

class FastCVScorer:
    """Fast CV scorer using rule-based algorithms with Windows compatibility"""
    
    def __init__(self):
        self.start_time = time.time()

    def score_cv(self, row: pd.Series) -> Dict[str, Any]:
        """Generate scores for a single CV using fast algorithms"""
        
        # Get data safely
        skills = str(row.get('skills_standardized', row.get('skills', ''))).strip()
        experience_years = float(row.get('total_experience_years', 0))
        education_level = str(row.get('education_level', 'NONE_SPECIFIED'))
        name = str(row.get('name', '')).strip()
        
        # Calculate component scores
        technical_score = self.calculate_technical_score(skills, experience_years)
        experience_score = self.calculate_experience_score(experience_years)
        education_score = self.calculate_education_score(education_level)
        skills_score = self.calculate_skills_score(skills)
        organization_score = self.calculate_organization_score(name, skills, experience_years)
        
        # Composite score (weighted average)
        composite_score = (
            technical_score * 0.3 +
            experience_score * 0.25 +
            education_score * 0.2 +
            skills_score * 0.15 +
            organization_score * 0.1
        )
        
        return {
            'organization_score': round(organization_score, 2),
            'technical_score': round(technical_score, 2),
            'composite_score': round(composite_score, 2),
            'experience_score': round(experience_score, 2),
            'skills_score': round(skills_score, 2),
            'education_score': round(education_score, 2)
        }

    def calculate_technical_score(self, skills: str, experience: float) -> float:
        """Calculate technical competency score"""
        if not skills or skills == 'nan':
            return 20.0
        
        skills_lower = skills.lower()
        
        # Count technical skills (safe ASCII keywords)
        technical_keywords = [
            'java', 'python', 'javascript', 'react', 'angular', 'vue',
            'node', 'spring', 'django', 'sql', 'docker', 'kubernetes',
            'aws', 'azure', 'git', 'html', 'css', 'php', 'cpp', 'csharp'
        ]
        
        skill_count = sum(1 for keyword in technical_keywords if keyword in skills_lower)
        
        # Base score from skill count
        base_score = min(skill_count * 8, 70)  # Max 70 from skills
        
        # Bonus from experience
        experience_bonus = min(experience * 3, 30)  # Max 30 from experience
        
        return min(base_score + experience_bonus, 100)

    def calculate_experience_score(self, experience_years: float) -> float:
        """Calculate experience score"""
        if experience_years <= 0:
            return 10.0
        elif experience_years < 1:
            return 30.0
        elif experience_years < 2:
            return 50.0
        elif experience_years < 5:
            return 70.0
        elif experience_years < 10:
            return 85.0
        else:
            return 95.0

    def calculate_education_score(self, education_level: str) -> float:
        """Calculate education score"""
        education_scores = {
            'PHD': 100.0,
            'MASTER': 85.0,
            'BACHELOR': 70.0,
            'ASSOCIATE': 55.0,
            'HIGH_SCHOOL': 40.0,
            'NONE_SPECIFIED': 25.0
        }
        return education_scores.get(education_level, 25.0)

    def calculate_skills_score(self, skills: str) -> float:
        """Calculate skills diversity and quality score"""
        if not skills or skills == 'nan':
            return 20.0
        
        # Count unique skills
        skill_list = [s.strip() for s in skills.split(',') if s.strip()]
        skill_count = len(skill_list)
        
        if skill_count == 0:
            return 20.0
        elif skill_count < 3:
            return 40.0
        elif skill_count < 6:
            return 60.0
        elif skill_count < 10:
            return 80.0
        else:
            return 95.0

    def calculate_organization_score(self, name: str, skills: str, experience: float) -> float:
        """Calculate CV organization score based on data completeness"""
        score = 50.0  # Base score
        
        # Name completeness
        if name and name != 'nan' and len(name) > 2:
            score += 15
        
        # Skills completeness
        if skills and skills != 'nan' and len(skills) > 10:
            score += 20
        
        # Experience data
        if experience > 0:
            score += 15
        
        return min(score, 100.0)

    def safe_print(self, message: str):
        """Safe print function for Windows encoding"""
        try:
            # Replace problematic Unicode characters
            safe_message = message.replace('≥', '>=').replace('≤', '<=')
            safe_message = re.sub(r'[^\x00-\x7F]+', '?', safe_message)
            print(safe_message)
        except UnicodeEncodeError:
            # Fallback to ASCII-only
            ascii_message = message.encode('ascii', 'ignore').decode('ascii')
            print(ascii_message)

    def process_csv(self, input_file: str, output_file: str):
        """Process CSV and generate scores"""
        try:
            self.safe_print(f"Scoring CVs from: {input_file}")
            
            # Read processed CV data with explicit encoding
            df = pd.read_csv(input_file, encoding='utf-8')
            self.safe_print(f"Loaded {len(df)} CVs for scoring")
            
            results = []
            
            # Score each CV
            for idx, row in df.iterrows():
                try:
                    user_id = row.get('user_id', f'user_{idx}')
                    job_offer_id = row.get('job_offer_id', f'job_{idx}')
                    
                    # Generate scores
                    scores = self.score_cv(row)
                    
                    # Create result record
                    result = {
                        'user_id': str(user_id),
                        'job_offer_id': str(job_offer_id),
                        'timestamp': datetime.now().isoformat(),
                        **scores
                    }
                    
                    results.append(result)
                    
                    self.safe_print(f"CV {idx+1}/{len(df)}: User {user_id}, Job {job_offer_id} - Score: {scores['composite_score']}")
                    
                except Exception as e:
                    self.safe_print(f"Error scoring CV {idx}: {e}")
                    continue
            
            # Save results as JSON with explicit encoding
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(results, f, indent=2, ensure_ascii=False)
            
            elapsed = time.time() - self.start_time
            self.safe_print(f"Scoring completed in {elapsed:.2f} seconds")
            self.safe_print(f"Results saved to: {output_file}")
            
            # Summary
            if results:
                avg_composite = sum(r['composite_score'] for r in results) / len(results)
                self.safe_print(f"Average composite score: {avg_composite:.1f}")
                high_scorers = len([r for r in results if r['composite_score'] >= 75])
                self.safe_print(f"High scorers (>=75): {high_scorers}/{len(results)}")
            
        except Exception as e:
            self.safe_print(f"Error: {e}")
            sys.exit(1)

def main():
    parser = argparse.ArgumentParser(description='Fast CV Scorer for Spring Integration')
    parser.add_argument('--input', required=True, help='Input CSV file path')
    parser.add_argument('--output', required=True, help='Output JSON file path')
    
    args = parser.parse_args()
    
    # Validate input file exists
    if not os.path.exists(args.input):
        print(f"Error: Input file not found: {args.input}")
        sys.exit(1)
    
    scorer = FastCVScorer()
    scorer.process_csv(args.input, args.output)

if __name__ == "__main__":
    main()