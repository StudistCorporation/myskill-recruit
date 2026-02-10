CREATE TABLE todo_lists (
  id            BIGSERIAL PRIMARY KEY,
  name          VARCHAR(255) NOT NULL,
  display_order INTEGER NOT NULL DEFAULT 0,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_todo_lists_timestamp
  BEFORE UPDATE ON todo_lists
  FOR EACH ROW EXECUTE PROCEDURE update_timestamp();
