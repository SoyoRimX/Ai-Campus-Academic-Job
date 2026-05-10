-- H2 测试数据
-- 默认密码: admin123 (BCrypt)
INSERT INTO sys_user (id, username, password, real_name, user_type, status) VALUES
(1, 'admin', '$2a$10$wsuQZy.Lk9w2jqlIOfSeC.TS3ZvAG0jdNpYMilCc1mhvDWo6LQi62', '系统管理员', 2, 1),
(2, 'teacher1', '$2a$10$wsuQZy.Lk9w2jqlIOfSeC.TS3ZvAG0jdNpYMilCc1mhvDWo6LQi62', '张老师', 1, 1),
(3, 'student1', '$2a$10$wsuQZy.Lk9w2jqlIOfSeC.TS3ZvAG0jdNpYMilCc1mhvDWo6LQi62', '李明', 0, 1),
(4, 'student2', '$2a$10$wsuQZy.Lk9w2jqlIOfSeC.TS3ZvAG0jdNpYMilCc1mhvDWo6LQi62', '王芳', 0, 1);

INSERT INTO sys_role (id, role_name, role_code, remark) VALUES
(1, '超级管理员', 'ROLE_ADMIN', '系统最高权限'),
(2, '教师', 'ROLE_TEACHER', '教师/辅导员权限'),
(3, '学生', 'ROLE_STUDENT', '学生权限');

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 3);

-- 学业数据
INSERT INTO academic_student (id, user_id, student_no, major, grade, class_name, gpa, total_credits, required_credits, fail_count, advisor, enrollment_year) VALUES
(1, 3, '2024001', '计算机科学与技术', '2024级', '计科2401', 3.6, 48.5, 60, 0, '张老师', 2024),
(2, 4, '2024002', '软件工程', '2024级', '软工2402', 2.8, 42.0, 60, 2, '张老师', 2024);

-- 课程
INSERT INTO academic_course (id, course_no, course_name, credit, course_type, semester, teacher) VALUES
(1, 'CS101', '数据结构与算法', 4.0, 1, '2024-2025-1', '王教授'),
(2, 'CS102', '操作系统', 3.5, 1, '2024-2025-1', '李教授'),
(3, 'CS201', '计算机网络', 3.0, 1, '2024-2025-2', '赵教授'),
(4, 'CS202', '数据库原理', 3.5, 1, '2024-2025-2', '陈教授'),
(5, 'PE101', '大学英语', 2.0, 3, '2024-2025-1', '刘老师');

-- 成绩
INSERT INTO academic_grade (student_id, course_id, score, grade_point, passed, semester, exam_type) VALUES
(1, 1, 92, 4.0, 1, '2024-2025-1', 1),
(1, 2, 85, 3.3, 1, '2024-2025-1', 1),
(1, 3, 78, 2.7, 1, '2024-2025-2', 1),
(2, 1, 55, 0, 0, '2024-2025-1', 1),
(2, 2, 60, 1.0, 1, '2024-2025-1', 2),
(2, 3, 45, 0, 0, '2024-2025-2', 1);

-- 预警
INSERT INTO academic_warning (id, student_id, warning_type, warning_level, title, description, is_read, is_handled) VALUES
(1, 2, 1, 2, '挂科预警：数据结构与算法', '学生王芳在数据结构与算法课程中成绩为55分，未通过考核。', 0, 0),
(2, 2, 3, 1, '学分预警：学分进度滞后', '当前已修42学分，距离应修60学分差距较大。', 0, 0);

-- 简历
INSERT INTO employ_resume (id, student_id, title, content, target_job, target_city, ai_score, is_default) VALUES
(1, 1, '前端开发工程师', '{"education":[{"school":"XX大学","major":"计算机科学与技术","period":"2024-2028"}],"internship":[{"company":"XX科技","role":"前端开发实习生","period":"2025.06-2025.09","desc":"参与React项目开发，负责组件开发和性能优化"}],"skills":["JavaScript","Vue.js","React","TypeScript","Node.js"],"selfIntro":"热爱前端技术，具备良好的学习能力和团队协作精神。"}', '前端开发', '深圳', 85, 1);

-- 岗位
INSERT INTO employ_job (id, job_title, company, salary_range, city, education, experience, required_skills, description, job_type, status, publisher_id) VALUES
(1, 'Java开发工程师', '腾讯科技', '15k-25k', '深圳', '本科', '1-3年', 'Java,Spring Boot,MySQL,Redis', '负责后台服务开发与维护，参与系统架构设计。', 1, 1, 2),
(2, '前端开发实习生', '字节跳动', '8k-12k', '北京', '本科', '应届', 'JavaScript,Vue.js,React,CSS', '参与产品前端页面开发，与设计团队协作。', 2, 1, 2),
(3, 'AI算法工程师', '阿里巴巴', '20k-35k', '杭州', '硕士', '1-3年', 'Python,TensorFlow,PyTorch,NLP', '负责大模型应用开发和算法优化。', 3, 1, 2);

-- 学习规划
INSERT INTO academic_study_plan (id, student_id, plan_title, semester, plan_detail, status) VALUES
(1, 1, '2024-2025-2 数据结构强化计划', '2024-2025-2', '1. 每周完成LeetCode 5道算法题\n2. 复习课本重点章节\n3. 参加ACM训练营', 0),
(2, 1, '2024-2025-2 英语四六级备考', '2024-2025-2', '1. 每天背50个单词\n2. 每周做2套真题\n3. 练习听力30分钟/天', 0),
(3, 2, '数据结构补考复习计划', '2024-2025-2', '1. 重点复习链表、树、图\n2. 完成课后习题\n3. 每周找老师答疑', 1);

-- 知识库
INSERT INTO ai_knowledge_base (id, title, content, category, tags, status) VALUES
(1, '计算机专业培养方案', '计算机科学与技术专业培养方案...', 'course', '培养方案,计算机', 1),
(2, '前端开发面试题库', '常见前端开发面试题及解答...', 'interview', '前端,面试', 1);
