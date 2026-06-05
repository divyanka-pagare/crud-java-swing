
-- Debugging

-- check the count of courses present in database like if its more than 1 means its duplicate  
SELECT course_name, COUNT(*)
FROM courses
GROUP BY course_name
HAVING COUNT(*) > 1;

SELECT * 
FROM courses
ORDER BY course_name;

DELETE c1 FROM courses c1
INNER JOIN courses c2 
WHERE 
    c1.id > c2.id
    AND c1.course_name = c2.course_name;
    
-- safe mode off 
SET SQL_SAFE_UPDATES = 0;

-- delete duplicates 
DELETE c1 FROM courses c1
INNER JOIN courses c2 
WHERE c1.id > c2.id
AND c1.course_name = c2.course_name;

-- safe mode on 
SET SQL_SAFE_UPDATES = 1;

-- it will never allowed duplicate entries for course in future 
ALTER TABLE courses
ADD CONSTRAINT unique_course UNIQUE (course_name, duration);