# Core utils
- Group:
  - id (UUID, PK)
  - name (String)
  - created_at (timestamp)
  - updated_at (timestamp)
  - invite_code (String, unique)
  - members (List of GroupMember)

- User:
  - id (UUID, PK)
  - email (String, unique)
  - username (String)
  - full_name (String)
  - password_hash (String)
  - is_active (Booelan, default: true)
  - created_at (timestamp)
  - updated_at (timestamp)
  - joined_at (timestamp, optional)

# Shopping

- Category:
  - id (UUID, PK)
  - group_id (UUID, FK -> Group)
  - name (String)
  - color (String, optional)
  - created_at (timestamp)
  - updated_at (timestamp)

- ShoppingList: (one per group)
  - group_id (UUID, PK, FK -> Group)
  - price (Decimal, optional)
  - updated_at (timestamp)
  - items (List of ShoppingItem)

- ShoppingItem:
  - id (UUID, PK)
  - group_id (UUID, FK -> Group)
  - category_id (UUID, FK -> Category, optional)
  - name (String)
  - quantity (Integer, default: 1)
  - created_at (timestamp)
  - updated_at (timestamp)

# Expenses

- ExpenseList:
  - group_id (UUID, PK, FK -> Group)
  - items (List of ExpenseItem)
  - updated_at (timestamp)

- ExpenseItem:
  - id (UUID, PK)
  - owner (FK -> User)
  - involved (List of User)
  - price (Decimal)
  - expense_date (timestamp)
  - created_at (timestamp)
  - updated_at (timestamp)

# Cleaning

- CleaningSchedule:
  - group_id (UUID; PK, FK -> Group)
  - tasks (List of CleaningTask)
  - updated_at (timestamp)

- CleaningTask:
  - id (UUID, PK)
  - title (String)
  - date (timestamp)
  - assigned_to (UUID, FK -> List of User)
  - ? recurrence_pattern (String, optional) 
  - completed (boolean, default: false)
  - created_at (timestamp)
  - updated_at (timestamp)

# Bulletin Board

- News:
  - id (UUID, PK)
  - group_id (UUID, FK -> Group)
  - actor_id (UUID, FK -> User)
  - type (Enum: MEMBER_ADDED, MEMBER_REMOVED, SHOPPING, EXPENSE_ADDED, EXPENSE_PAID, CLEANING_TASK_ADDED)
  - name (String)
  - description (String)
  - created_at (timestamp)
