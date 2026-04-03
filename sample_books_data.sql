-- 多类别图书示例数据
-- 使用前请确保 book_categories 已按 schema.sql 初始化

SET @admin_user_id = (SELECT user_id FROM users WHERE username = 'admin' LIMIT 1);
SET @cat_A = (SELECT category_id FROM book_categories WHERE category_code = 'A' LIMIT 1);
SET @cat_B = (SELECT category_id FROM book_categories WHERE category_code = 'B' LIMIT 1);
SET @cat_C = (SELECT category_id FROM book_categories WHERE category_code = 'C' LIMIT 1);
SET @cat_D = (SELECT category_id FROM book_categories WHERE category_code = 'D' LIMIT 1);
SET @cat_E = (SELECT category_id FROM book_categories WHERE category_code = 'E' LIMIT 1);
SET @cat_F = (SELECT category_id FROM book_categories WHERE category_code = 'F' LIMIT 1);
SET @cat_G = (SELECT category_id FROM book_categories WHERE category_code = 'G' LIMIT 1);
SET @cat_H = (SELECT category_id FROM book_categories WHERE category_code = 'H' LIMIT 1);
SET @cat_I = (SELECT category_id FROM book_categories WHERE category_code = 'I' LIMIT 1);
SET @cat_J = (SELECT category_id FROM book_categories WHERE category_code = 'J' LIMIT 1);
SET @cat_K = (SELECT category_id FROM book_categories WHERE category_code = 'K' LIMIT 1);
SET @cat_N = (SELECT category_id FROM book_categories WHERE category_code = 'N' LIMIT 1);
SET @cat_O = (SELECT category_id FROM book_categories WHERE category_code = 'O' LIMIT 1);
SET @cat_P = (SELECT category_id FROM book_categories WHERE category_code = 'P' LIMIT 1);
SET @cat_Q = (SELECT category_id FROM book_categories WHERE category_code = 'Q' LIMIT 1);
SET @cat_R = (SELECT category_id FROM book_categories WHERE category_code = 'R' LIMIT 1);
SET @cat_S = (SELECT category_id FROM book_categories WHERE category_code = 'S' LIMIT 1);
SET @cat_T = (SELECT category_id FROM book_categories WHERE category_code = 'T' LIMIT 1);
SET @cat_U = (SELECT category_id FROM book_categories WHERE category_code = 'U' LIMIT 1);
SET @cat_V = (SELECT category_id FROM book_categories WHERE category_code = 'V' LIMIT 1);
SET @cat_X = (SELECT category_id FROM book_categories WHERE category_code = 'X' LIMIT 1);
SET @cat_Z = (SELECT category_id FROM book_categories WHERE category_code = 'Z' LIMIT 1);

INSERT INTO books (
    book_name, author, isbn, publisher, publish_date, page_count, price, description,
    category_id, tags, shelf_location, barcode, call_number, entry_date, source,
    collection_status, is_borrowable, creator_id
) VALUES
('马克思主义基本原理导论', '刘建军', '9787010000001', '人民出版社', '2021-03-01', 320, 58.00, '适合思想政治与马克思主义理论课程使用。', @cat_A, '马克思主义,理论', 'A-01-01', 'BK000001', 'A11/001', '2024-03-01', 'purchase', 'on_shelf', 1, @admin_user_id),
('中国特色社会主义理论专题', '韩庆祥', '9787010000002', '学习出版社', '2022-06-15', 286, 49.00, '围绕中国特色社会主义理论体系展开。', @cat_A, '中国特色社会主义,理论', 'A-01-02', 'BK000002', 'A12/002', '2024-03-01', 'purchase', 'on_shelf', 1, @admin_user_id),
('中国哲学十五讲', '杨立华', '9787010000003', '北京大学出版社', '2020-09-10', 298, 46.00, '中国哲学史入门读物。', @cat_B, '哲学,中国哲学', 'B-01-01', 'BK000003', 'B20/003', '2024-03-02', 'purchase', 'on_shelf', 1, @admin_user_id),
('宗教学概论', '卓新平', '9787010000004', '中国社会科学出版社', '2019-12-01', 342, 52.00, '宗教基础知识与研究方法。', @cat_B, '宗教,人文', 'B-01-02', 'BK000004', 'B90/004', '2024-03-02', 'purchase', 'on_shelf', 1, @admin_user_id),
('社会调查研究方法', '风笑天', '9787010000005', '中国人民大学出版社', '2022-02-20', 410, 68.00, '社会科学研究方法经典教材。', @cat_C, '社会学,研究方法', 'C-01-01', 'BK000005', 'C91/005', '2024-03-03', 'purchase', 'on_shelf', 1, @admin_user_id),
('公共管理学导论', '陈振明', '9787010000006', '中国人民大学出版社', '2021-08-08', 356, 59.00, '公共管理基础理论与案例。', @cat_C, '公共管理,管理学', 'C-01-02', 'BK000006', 'C93/006', '2024-03-03', 'purchase', 'on_shelf', 1, @admin_user_id),
('法理学教程', '张文显', '9787010000007', '高等教育出版社', '2023-01-12', 388, 62.00, '法学专业基础课程教材。', @cat_D, '法律,法理学', 'D-01-01', 'BK000007', 'D90/007', '2024-03-04', 'purchase', 'on_shelf', 1, @admin_user_id),
('中国政治制度史', '周振鹤', '9787010000008', '中华书局', '2020-05-18', 330, 55.00, '梳理中国古今政治制度演变。', @cat_D, '政治,制度史', 'D-01-02', 'BK000008', 'D62/008', '2024-03-04', 'purchase', 'on_shelf', 1, @admin_user_id),
('国防概论', '王保存', '9787010000009', '国防大学出版社', '2021-10-10', 304, 43.00, '高校国防教育通识教材。', @cat_E, '军事,国防', 'E-01-01', 'BK000009', 'E0/009', '2024-03-05', 'purchase', 'on_shelf', 1, @admin_user_id),
('现代军事科技', '徐焰', '9787010000010', '解放军出版社', '2019-06-30', 280, 48.00, '介绍现代军事科技发展。', @cat_E, '军事,科技', 'E-01-02', 'BK000010', 'E92/010', '2024-03-05', 'purchase', 'on_shelf', 1, @admin_user_id),
('宏观经济学原理', '高鸿业', '9787010000011', '中国人民大学出版社', '2022-07-01', 412, 69.00, '经济学核心课程教材。', @cat_F, '经济学,宏观经济', 'F-01-01', 'BK000011', 'F015/011', '2024-03-06', 'purchase', 'on_shelf', 1, @admin_user_id),
('金融学基础', '黄达', '9787010000012', '中国金融出版社', '2021-11-15', 396, 66.00, '适合金融学专业基础教学。', @cat_F, '金融,经济', 'F-01-02', 'BK000012', 'F830/012', '2024-03-06', 'purchase', 'on_shelf', 1, @admin_user_id),
('教育学原理', '叶澜', '9787010000013', '人民教育出版社', '2020-03-25', 368, 57.00, '师范类教育学基础教材。', @cat_G, '教育学,师范', 'G-01-01', 'BK000013', 'G40/013', '2024-03-07', 'purchase', 'on_shelf', 1, @admin_user_id),
('体育运动训练学', '田麦久', '9787010000014', '高等教育出版社', '2018-09-01', 344, 53.00, '运动训练理论与实践。', @cat_G, '体育,训练', 'G-01-02', 'BK000014', 'G808/014', '2024-03-07', 'purchase', 'on_shelf', 1, @admin_user_id),
('现代汉语', '黄伯荣', '9787010000015', '高等教育出版社', '2022-09-01', 438, 72.00, '汉语言文学专业核心教材。', @cat_H, '汉语,语言学', 'H-01-01', 'BK000015', 'H109/015', '2024-03-08', 'purchase', 'on_shelf', 1, @admin_user_id),
('英语写作教程', '王振亚', '9787010000016', '外语教学与研究出版社', '2021-04-12', 260, 42.00, '大学英语写作训练教材。', @cat_H, '英语,写作', 'H-01-02', 'BK000016', 'H315/016', '2024-03-08', 'purchase', 'on_shelf', 1, @admin_user_id),
('中国现当代文学史', '钱理群', '9787010000017', '北京大学出版社', '2020-10-01', 455, 76.00, '文学专业基础课程参考书。', @cat_I, '文学史,中国文学', 'I-01-01', 'BK000017', 'I209/017', '2024-03-09', 'purchase', 'on_shelf', 1, @admin_user_id),
('平凡的世界', '路遥', '9787010000018', '作家出版社', '2017-01-01', 1250, 108.00, '当代中国现实主义长篇小说。', @cat_I, '小说,现实主义', 'I-01-02', 'BK000018', 'I247/018', '2024-03-09', 'donation', 'on_shelf', 1, @admin_user_id),
('艺术概论', '彭吉象', '9787010000019', '北京大学出版社', '2021-02-02', 310, 51.00, '艺术学概论类教材。', @cat_J, '艺术学,概论', 'J-01-01', 'BK000019', 'J0/019', '2024-03-10', 'purchase', 'on_shelf', 1, @admin_user_id),
('中国美术史', '洪再新', '9787010000020', '中国美术学院出版社', '2019-07-07', 360, 63.00, '中国美术发展脉络梳理。', @cat_J, '美术史,艺术', 'J-01-02', 'BK000020', 'J120/020', '2024-03-10', 'purchase', 'on_shelf', 1, @admin_user_id),
('中国古代史纲要', '张帆', '9787010000021', '北京大学出版社', '2022-03-18', 420, 70.00, '历史学专业基础教材。', @cat_K, '历史,中国史', 'K-01-01', 'BK000021', 'K20/021', '2024-03-11', 'purchase', 'on_shelf', 1, @admin_user_id),
('世界地理教程', '赵荣', '9787010000022', '高等教育出版社', '2020-08-20', 350, 56.00, '地理科学专业通识教材。', @cat_K, '地理,世界地理', 'K-01-02', 'BK000022', 'K90/022', '2024-03-11', 'purchase', 'on_shelf', 1, @admin_user_id),
('自然科学概论', '任定成', '9787010000023', '科学出版社', '2021-12-12', 300, 47.00, '自然科学入门与科学精神教育。', @cat_N, '自然科学,通识', 'N-01-01', 'BK000023', 'N49/023', '2024-03-12', 'purchase', 'on_shelf', 1, @admin_user_id),
('高等数学基础', '同济大学数学系', '9787010000024', '高等教育出版社', '2023-02-01', 520, 79.00, '理工科高等数学教材。', @cat_O, '数学,高等数学', 'O-01-01', 'BK000024', 'O13/024', '2024-03-12', 'purchase', 'on_shelf', 1, @admin_user_id),
('大学物理学', '程守洙', '9787010000025', '高等教育出版社', '2022-09-09', 460, 74.00, '理工科大学物理课程教材。', @cat_O, '物理,大学物理', 'O-01-02', 'BK000025', 'O4/025', '2024-03-12', 'purchase', 'on_shelf', 1, @admin_user_id),
('地球科学导论', '陈骏', '9787010000026', '地质出版社', '2020-11-11', 332, 58.00, '地球科学基本概念与方法。', @cat_P, '地球科学,地质', 'P-01-01', 'BK000026', 'P18/026', '2024-03-13', 'purchase', 'on_shelf', 1, @admin_user_id),
('天文学新概论', '苏宜', '9787010000027', '科学出版社', '2019-09-19', 290, 54.00, '介绍天文学基本知识与观测。', @cat_P, '天文学,宇宙', 'P-01-02', 'BK000027', 'P1/027', '2024-03-13', 'purchase', 'on_shelf', 1, @admin_user_id),
('普通生物学', '吴相钰', '9787010000028', '高等教育出版社', '2021-05-05', 420, 71.00, '生命科学基础课程教材。', @cat_Q, '生物学,生命科学', 'Q-01-01', 'BK000028', 'Q1/028', '2024-03-14', 'purchase', 'on_shelf', 1, @admin_user_id),
('遗传学基础', '朱军', '9787010000029', '科学出版社', '2022-01-06', 366, 64.00, '遗传学核心概念与实验方法。', @cat_Q, '遗传学,生物', 'Q-01-02', 'BK000029', 'Q3/029', '2024-03-14', 'purchase', 'on_shelf', 1, @admin_user_id),
('临床医学概论', '葛均波', '9787010000030', '人民卫生出版社', '2022-04-04', 390, 73.00, '医学专业基础导论。', @cat_R, '医学,临床', 'R-01-01', 'BK000030', 'R4/030', '2024-03-15', 'purchase', 'on_shelf', 1, @admin_user_id),
('护理学基础', '李小寒', '9787010000031', '人民卫生出版社', '2021-07-17', 410, 69.00, '护理专业核心基础教材。', @cat_R, '护理,医学', 'R-01-02', 'BK000031', 'R47/031', '2024-03-15', 'purchase', 'on_shelf', 1, @admin_user_id),
('农业生态学', '骆世明', '9787010000032', '中国农业出版社', '2020-06-16', 315, 52.00, '农业生态系统基础理论。', @cat_S, '农业,生态', 'S-01-01', 'BK000032', 'S18/032', '2024-03-16', 'purchase', 'on_shelf', 1, @admin_user_id),
('园艺植物栽培学', '胡桂兵', '9787010000033', '中国农业大学出版社', '2019-03-21', 288, 48.00, '园艺与植物栽培实务教材。', @cat_S, '园艺,栽培', 'S-01-02', 'BK000033', 'S60/033', '2024-03-16', 'purchase', 'on_shelf', 1, @admin_user_id),
('计算机网络', '谢希仁', '9787010000034', '电子工业出版社', '2023-03-03', 430, 78.00, '计算机专业经典教材。', @cat_T, '计算机,网络', 'T-01-01', 'BK000034', 'TP393/034', '2024-03-17', 'purchase', 'on_shelf', 1, @admin_user_id),
('数据库系统概论', '王珊', '9787010000035', '高等教育出版社', '2022-12-12', 418, 75.00, '数据库课程教材与案例。', @cat_T, '数据库,计算机', 'T-01-02', 'BK000035', 'TP311/035', '2024-03-17', 'purchase', 'on_shelf', 1, @admin_user_id),
('交通运输工程学', '王炜', '9787010000036', '人民交通出版社', '2021-09-09', 342, 59.00, '交通运输工程专业教材。', @cat_U, '交通,工程', 'U-01-01', 'BK000036', 'U11/036', '2024-03-18', 'purchase', 'on_shelf', 1, @admin_user_id),
('轨道交通概论', '陈湘生', '9787010000037', '中国铁道出版社', '2020-02-28', 305, 49.00, '城市轨道交通基础知识。', @cat_U, '轨道交通,铁路', 'U-01-02', 'BK000037', 'U239/037', '2024-03-18', 'purchase', 'on_shelf', 1, @admin_user_id),
('航空航天概论', '杨俊华', '9787010000038', '北京航空航天大学出版社', '2022-08-08', 326, 61.00, '航空航天基础通识教材。', @cat_V, '航空,航天', 'V-01-01', 'BK000038', 'V11/038', '2024-03-19', 'purchase', 'on_shelf', 1, @admin_user_id),
('飞行器设计基础', '王华明', '9787010000039', '国防工业出版社', '2021-01-20', 372, 68.00, '飞行器设计与工程基础。', @cat_V, '飞行器,设计', 'V-01-02', 'BK000039', 'V22/039', '2024-03-19', 'purchase', 'on_shelf', 1, @admin_user_id),
('环境科学导论', '左玉辉', '9787010000040', '高等教育出版社', '2021-06-06', 340, 57.00, '环境科学基础知识体系。', @cat_X, '环境科学,生态', 'X-01-01', 'BK000040', 'X1/040', '2024-03-20', 'purchase', 'on_shelf', 1, @admin_user_id),
('安全工程学', '罗云', '9787010000041', '化学工业出版社', '2020-10-10', 360, 60.00, '安全工程专业核心教材。', @cat_X, '安全工程,工程', 'X-01-02', 'BK000041', 'X92/041', '2024-03-20', 'purchase', 'on_shelf', 1, @admin_user_id),
('百科知识读本', '国家图书馆编', '9787010000042', '商务印书馆', '2018-05-05', 280, 45.00, '综合性知识读物，适合通识阅读。', @cat_Z, '百科,通识', 'Z-01-01', 'BK000042', 'Z22/042', '2024-03-21', 'donation', 'on_shelf', 1, @admin_user_id),
('大学生科研写作指南', '王晓梅', '9787010000043', '清华大学出版社', '2023-04-18', 260, 39.00, '论文写作与科研规范指导。', @cat_Z, '科研,写作', 'Z-01-02', 'BK000043', 'Z5/043', '2024-03-21', 'purchase', 'on_shelf', 1, @admin_user_id);
