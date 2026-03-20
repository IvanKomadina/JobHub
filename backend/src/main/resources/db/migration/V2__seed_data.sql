-- ============================================================
-- V2 - Seed Data
-- ============================================================

-- ==================== CATEGORIES ====================

INSERT INTO categories (name, slug) VALUES
    ('Information Technology', 'information-technology'),
    ('Marketing',              'marketing'),
    ('Finance',                'finance'),
    ('Healthcare',             'healthcare'),
    ('Engineering',            'engineering'),
    ('Education',              'education'),
    ('Sales',                  'sales'),
    ('Design',                 'design'),
    ('Human Resources',        'human-resources'),
    ('Customer Support',       'customer-support');

-- ==================== LOCATIONS ====================

INSERT INTO locations (city, country) VALUES
    ('Zagreb',   'Croatia'),
    ('Split',    'Croatia'),
    ('Rijeka',   'Croatia'),
    ('Osijek',   'Croatia'),
    ('Varaždin', 'Croatia'),
    ('Remote',   'Worldwide');

-- ==================== DEFAULT ADMIN ACCOUNT ====================
-- Password: Admin1234!
-- IMPORTANT: Change this password immediately after first login!

INSERT INTO users (email, password_hash, role, is_active)
VALUES (
    'admin@jobhub.com',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'ADMINISTRATOR',
    TRUE
);
