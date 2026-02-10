INSERT INTO todo_items (id, todo_list_id, content, done, display_order, due_date) VALUES
  (1, 1, '牛乳を買う', false, 0, '2026-02-10'),
  (2, 1, 'パンを買う', true, 1, NULL),
  (3, 1, '卵を買う', false, 2, '2026-02-15'),
  (4, 2, '週報を書く', false, 0, '2026-02-14'),
  (5, 2, 'コードレビュー', true, 1, NULL),
  (6, 2, 'ミーティング準備', false, 2, '2026-02-12'),
  (7, 3, 'プログラミングClojure', false, 0, NULL),
  (8, 3, 'リファクタリング', true, 1, NULL),
  (9, 3, 'Clean Architecture', false, 2, NULL);
