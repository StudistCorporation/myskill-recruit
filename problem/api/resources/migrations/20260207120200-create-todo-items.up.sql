CREATE TABLE todo_items (
  id            BIGSERIAL PRIMARY KEY,
  todo_list_id  BIGINT NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
  content       TEXT NOT NULL DEFAULT '',
  done          BOOLEAN NOT NULL DEFAULT FALSE,
  due_date      DATE,
  display_order INTEGER NOT NULL DEFAULT 0,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_todo_items_list_order ON todo_items(todo_list_id, display_order);

CREATE TRIGGER update_todo_items_timestamp
  BEFORE UPDATE ON todo_items
  FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
