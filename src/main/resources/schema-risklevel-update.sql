-- SQL Migration Script to convert legacy 'UNKNOWN' risk levels to 'MODERATE'

UPDATE positions 
SET risk_level = 'MODERATE' 
WHERE risk_level = 'UNKNOWN' OR risk_level IS NULL;

UPDATE portfolios 
SET aggregated_risk_level = 'MODERATE' 
WHERE aggregated_risk_level = 'UNKNOWN' OR aggregated_risk_level IS NULL;
