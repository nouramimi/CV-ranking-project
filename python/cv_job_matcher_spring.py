#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import pandas as pd
import argparse
import json
import sys
import time
import os
import psycopg2
from psycopg2 import sql
from datetime import datetime

# Fix Windows encoding issues
import codecs
if sys.platform.startswith('win'):
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout.detach())
    sys.stderr = codecs.getwriter('utf-8')(sys.stderr.detach())

class CVJobMatcher:
    """CV Job Matcher avec requête SQL corrigée"""
    
    def __init__(self, db_config):
        self.start_time = time.time()
        self.db_config = db_config

    def safe_print(self, message):
        """Safe print function for Windows encoding"""
        try:
            safe_message = message.replace('≥', '>=').replace('≤', '<=')
            safe_message = safe_message.encode('ascii', 'ignore').decode('ascii')
            print(safe_message)
        except:
            print(message.encode('ascii', 'ignore').decode('ascii'))

    def fetch_job_requirements(self, job_offer_id):
        """Fetch job requirements avec la bonne structure de table"""
        try:
            conn = psycopg2.connect(**self.db_config)
            cursor = conn.cursor()
            
            # REQUÊTE CORRIGÉE selon votre entité Java
            main_query = sql.SQL("""
                SELECT 
                    title,
                    description,
                    years_of_experience_required,
                    required_degree,
                    min_salary,
                    max_salary,
                    employment_type,
                    location
                FROM job_offer
                WHERE id = %s
            """)
            
            cursor.execute(main_query, (job_offer_id,))
            main_result = cursor.fetchone()
            
            if not main_result:
                self.safe_print(f"No job offer found with ID: {job_offer_id}")
                cursor.close()
                conn.close()
                return None
            
            # Query pour les skills
            skills_query = sql.SQL("""
                SELECT skill
                FROM job_offer_skills
                WHERE job_offer_id = %s
            """)
            
            cursor.execute(skills_query, (job_offer_id,))
            skills_results = cursor.fetchall()
            required_skills = [skill[0].strip().lower() for skill in skills_results] if skills_results else []
            
            cursor.close()
            conn.close()
            
            requirements = {
                'title': main_result[0] or 'Unknown Position',
                'description': main_result[1] or '',
                'min_experience': main_result[2] or 0,
                'required_degree': main_result[3] or 'NONE_SPECIFIED',
                'salary_min': main_result[4] or 0,
                'salary_max': main_result[5] or 0,
                'employment_type': main_result[6] or '',
                'location': main_result[7] or '',
                'required_skills': required_skills
            }
            
            self.safe_print(f"SUCCESS: Found job '{requirements['title']}'")
            self.safe_print(f"  Experience required: {requirements['min_experience']} years")
            self.safe_print(f"  Required degree: {requirements['required_degree']}")
            self.safe_print(f"  Employment type: {requirements['employment_type']}")
            self.safe_print(f"  Location: {requirements['location']}")
            self.safe_print(f"  Required skills: {len(required_skills)} skills")
            if required_skills:
                self.safe_print(f"  Skills: {', '.join(required_skills[:5])}")
            
            return requirements
            
        except Exception as e:
            self.safe_print(f"Database error for job {job_offer_id}: {e}")
            return None

    def calculate_matching_scores(self, cv_row, job_requirements):
        """Calculate matching scores based on CV and job requirements"""
        
        # Get CV data
        cv_skills = str(cv_row.get('skills_standardized', cv_row.get('skills', ''))).lower()
        cv_experience = float(cv_row.get('total_experience_years', 0))
        cv_education = str(cv_row.get('education_level', 'NONE_SPECIFIED'))
        
        # Calculate skills match
        skills_match = self.calculate_skills_match(cv_skills, job_requirements['required_skills'])
        
        # Calculate experience match
        experience_match = self.calculate_experience_match(cv_experience, job_requirements['min_experience'])
        
        # Calculate education match
        education_match = self.calculate_education_match(cv_education, job_requirements['required_degree'])
        
        # Calculate content relevance
        content_relevance = self.calculate_content_relevance(cv_row, job_requirements)
        
        # Overall score (weighted average)
        overall_score = (
            skills_match['skills_match_score'] * 0.40 +
            experience_match['experience_match_score'] * 0.25 +
            education_match['education_match_score'] * 0.20 +
            content_relevance['content_relevance_score'] * 0.15
        )
        
        # Determine match level
        if overall_score >= 85:
            match_level = 'EXCELLENT'
        elif overall_score >= 70:
            match_level = 'GOOD'
        elif overall_score >= 55:
            match_level = 'FAIR'
        else:
            match_level = 'POOR'
        
        return {
            'overall_match_score': round(overall_score, 2),
            'match_level': match_level,
            'skills_match': skills_match,
            'experience_match': experience_match,
            'education_match': education_match,
            'content_relevance': content_relevance,
            'job_title': job_requirements['title'],
            'analysis_timestamp': datetime.now().isoformat()
        }

    def calculate_skills_match(self, cv_skills, required_skills):
        """Calculate skills matching score"""
        if not cv_skills or not required_skills:
            return {
                'skills_match_score': 50.0,
                'matched_skills_count': 0,
                'required_skills_count': len(required_skills),
                'skills_coverage_percentage': 0.0
            }
        
        cv_skills_list = [s.strip() for s in cv_skills.split(',') if s.strip()]
        matched_count = 0
        
        for required_skill in required_skills:
            for cv_skill in cv_skills_list:
                if required_skill.lower() in cv_skill.lower() or cv_skill.lower() in required_skill.lower():
                    matched_count += 1
                    break
        
        if len(required_skills) > 0:
            coverage = (matched_count / len(required_skills)) * 100
            score = min(50 + coverage * 0.5, 100)  # Base 50 + bonus based on coverage
        else:
            coverage = 100.0
            score = 75.0
        
        return {
            'skills_match_score': round(score, 2),
            'matched_skills_count': matched_count,
            'required_skills_count': len(required_skills),
            'skills_coverage_percentage': round(coverage, 2)
        }

    def calculate_experience_match(self, cv_experience, required_experience):
        """Calculate experience matching score"""
        if required_experience <= 0:
            return {
                'experience_match_score': 100.0,
                'experience_gap': 0.0,
                'experience_status': 'No requirement'
            }
        
        if cv_experience >= required_experience:
            # Bonus for extra experience
            bonus = min((cv_experience - required_experience) * 5, 20)
            score = min(80 + bonus, 100)
            status = 'Exceeds requirement'
            gap = 0.0
        else:
            # Penalty for insufficient experience
            ratio = cv_experience / required_experience if required_experience > 0 else 0
            score = ratio * 80
            status = 'Below requirement'
            gap = required_experience - cv_experience
        
        return {
            'experience_match_score': round(score, 2),
            'experience_gap': round(gap, 2),
            'experience_status': status
        }

    def calculate_education_match(self, cv_education, required_education):
        """Calculate education matching score"""
        education_hierarchy = {
            'PHD': 5, 'MASTER': 4, 'BACHELOR': 3, 
            'ASSOCIATE': 2, 'HIGH_SCHOOL': 1, 'NONE_SPECIFIED': 0
        }
        
        cv_level = education_hierarchy.get(cv_education, 0)
        required_level = education_hierarchy.get(required_education, 0)
        
        if required_level == 0:
            return {
                'education_match_score': 100.0,
                'education_status': 'No requirement'
            }
        
        if cv_level >= required_level:
            bonus = (cv_level - required_level) * 10
            score = min(80 + bonus, 100)
            status = 'Meets or exceeds requirement'
        else:
            score = (cv_level / required_level) * 70 if required_level > 0 else 0
            status = 'Below requirement'
        
        return {
            'education_match_score': round(score, 2),
            'education_status': status
        }

    def calculate_content_relevance(self, cv_row, job_requirements):
        """Calculate content relevance score"""
        # Simple keyword matching
        job_title = job_requirements['title'].lower()
        job_desc = job_requirements['description'].lower()
        
        cv_content = (
            str(cv_row.get('description', '')) + ' ' +
            str(cv_row.get('skills', '')) + ' ' +
            str(cv_row.get('experience', ''))
        ).lower()
        
        # Extract keywords from job
        job_keywords = []
        if 'java' in job_title or 'java' in job_desc:
            job_keywords.append('java')
        if 'senior' in job_title or 'senior' in job_desc:
            job_keywords.append('senior')
        if 'developer' in job_title or 'developer' in job_desc:
            job_keywords.append('developer')
        if 'spring' in job_desc:
            job_keywords.append('spring')
        
        # Check matches
        matched_keywords = []
        for keyword in job_keywords:
            if keyword in cv_content:
                matched_keywords.append(keyword)
        
        if job_keywords:
            score = (len(matched_keywords) / len(job_keywords)) * 100
        else:
            score = 75.0  # Default if no keywords extracted
        
        return {
            'content_relevance_score': round(score, 2),
            'matched_keywords': matched_keywords,
            'total_keywords': len(job_keywords)
        }

    def process_csv(self, input_file, output_file):
        """Process CSV and generate matching scores"""
        try:
            self.safe_print(f"Starting job matching analysis from: {input_file}")
            
            # Read CSV
            df = pd.read_csv(input_file, encoding='utf-8')
            self.safe_print(f"Loaded {len(df)} CVs for job matching analysis")
            
            results = []
            
            # Process each CV
            for idx, row in df.iterrows():
                try:
                    user_id = row.get('user_id', f'user_{idx}')
                    job_offer_id = str(row.get('job_offer_id', f'job_{idx}'))
                    
                    self.safe_print(f"Processing CV {idx+1}/{len(df)}: User {user_id}, Job {job_offer_id}")
                    
                    # Get job requirements
                    job_requirements = self.fetch_job_requirements(job_offer_id)
                    
                    if not job_requirements:
                        self.safe_print(f"  Skipping - no job requirements found")
                        continue
                    
                    # Calculate matching scores
                    match_analysis = self.calculate_matching_scores(row, job_requirements)
                    
                    # Create result
                    result = {
                        'user_id': str(user_id),
                        'job_offer_id': job_offer_id,
                        **match_analysis
                    }
                    
                    results.append(result)
                    
                    self.safe_print(f"  SUCCESS: Job '{match_analysis['job_title']}'")
                    self.safe_print(f"  Match Score: {match_analysis['overall_match_score']}/100")
                    self.safe_print(f"  Match Level: {match_analysis['match_level']}")
                    
                except Exception as e:
                    self.safe_print(f"Error processing CV {idx}: {e}")
                    continue
            
            # Save results
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(results, f, indent=2, ensure_ascii=False)
            
            elapsed = time.time() - self.start_time
            self.safe_print(f"Job matching completed in {elapsed:.2f} seconds")
            self.safe_print(f"Results saved to: {output_file}")
            
            # Summary
            if results:
                avg_score = sum(r['overall_match_score'] for r in results) / len(results)
                self.safe_print(f"Summary: {len(results)} CVs processed, avg score: {avg_score:.1f}")
            else:
                self.safe_print("No results to summarize")
            
        except Exception as e:
            self.safe_print(f"Error: {e}")
            sys.exit(1)

def main():
    parser = argparse.ArgumentParser(description='CV Job Matcher')
    parser.add_argument('--input', required=True, help='Input CSV file')
    parser.add_argument('--output', required=True, help='Output JSON file')
    parser.add_argument('--db-host', default='localhost', help='Database host')
    parser.add_argument('--db-name', default='cv_filter', help='Database name')
    parser.add_argument('--db-user', default='postgres', help='Database user')
    parser.add_argument('--db-password', default='2003', help='Database password')
    parser.add_argument('--db-port', default='5432', help='Database port')
    
    args = parser.parse_args()
    
    if not os.path.exists(args.input):
        print(f"Error: Input file not found: {args.input}")
        sys.exit(1)
    
    db_config = {
        'host': args.db_host,
        'database': args.db_name,
        'user': args.db_user,
        'password': args.db_password,
        'port': args.db_port
    }
    
    matcher = CVJobMatcher(db_config)
    matcher.process_csv(args.input, args.output)

if __name__ == "__main__":
    main()