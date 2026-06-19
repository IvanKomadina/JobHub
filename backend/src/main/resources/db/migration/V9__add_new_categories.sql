INSERT INTO categories (name, slug) VALUES
                                        ('Operations', 'operations'),
                                        ('Administration', 'administration'),
                                        ('Legal', 'legal'),
                                        ('Construction', 'construction'),
                                        ('Hospitality', 'hospitality')
    ON CONFLICT (slug) DO NOTHING;