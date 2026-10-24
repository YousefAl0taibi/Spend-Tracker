-- Categories needed once MCC-based categorisation (see MccCategoryLookup) started
-- resolving real card transactions to categories the original rules.yml never covered.
INSERT INTO category (name_en, name_ar) VALUES
    ('Shopping', 'تسوق'),
    ('Health', 'صحة'),
    ('Transport', 'تنقل'),
    ('Automotive', 'سيارات'),
    ('Pets', 'حيوانات'),
    ('Entertainment', 'ترفيه');
