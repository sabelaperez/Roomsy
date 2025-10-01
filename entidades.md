# Core utils
- Group:
  - id (UUID, PK)
  - name (String)
  - created_at (timestamp)
  - updated_at (timestamp)
  - invite_code (String, unique)

- User:
  - id (UUID, PK)
  - email (String, unique)
  - username (String)
  - full_name (String)
  - password_hash (String)
  - is_active (Booelan, default: true)
  - created_at (timestamp)
  - updated_at (timestamp)

- GroupMember:
  - id (UUID, PK)
  - group_id (UUID, FK -> Group)
  - user_id (UUID, FK -> User)
  - joined_at (timestamp)

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
  - category_id (UUID, FK -> Category)
  - added_by (UUID, FK -> User)
  - name (String)
  - quantity (Integer, default: 1)
  - is_purchased (Boolean, default: false)
  - priority (Enum: low, medium, high. Optional)
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
  - created_by (UUID, FK -> User)
  - assigned_to (UUID, FK -> User, optional)
  - recurrence_pattern (String, optional) 
  - completed_at (timestamp)
  - created_at (timestamp)
  - updated_at (timestamp)
