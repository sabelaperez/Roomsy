import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { shoppingApi, categoryApi, groupApi } from '../api';

export default function ShoppingItems() {
  const { user } = useContext(AuthContext);
  const [group, setGroup] = useState(null);
  const [loadingGroup, setLoadingGroup] = useState(true);

  const [items, setItems] = useState([]);
  const [loadingItems, setLoadingItems] = useState(false);

  const [categories, setCategories] = useState([]);
  const [loadingCategories, setLoadingCategories] = useState(false);

  const [itemForm, setItemForm] = useState({
    name: '',
    quantity: '',
    categoryId: ''
  });
  const [itemFormError, setItemFormError] = useState('');
  const [submittingItem, setSubmittingItem] = useState(false);

  const [categoryForm, setCategoryForm] = useState({
    name: '',
    color: '#3B82F6'
  });
  const [categoryFormError, setCategoryFormError] = useState('');
  const [submittingCategory, setSubmittingCategory] = useState(false);

  const [editingItem, setEditingItem] = useState(null);
  const [editingCategory, setEditingCategory] = useState(null);

  useEffect(() => {
    if (!user) return;
    let mounted = true;
    const loadGroup = async () => {
      setLoadingGroup(true);
      try {
        const g = await groupApi.get(user.userId);
        if (!mounted) return;
        setGroup(g);
      } catch (e) {
        console.error('Failed to load group:', e);
        setGroup(null);
      } finally {
        if (mounted) setLoadingGroup(false);
      }
    };
    loadGroup();
    return () => { mounted = false; };
  }, [user]);

  useEffect(() => {
    if (!group?.id) return;
    loadItems();
    loadCategories();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [group?.id]);

  const loadItems = async () => {
    if (!group?.id) return;
    setLoadingItems(true);
    try {
      const data = await shoppingApi.getGroupShoppingItems(group.id, { page: 0, size: 100 });
      setItems(data?.content ?? data?.items ?? []);
    } catch (e) {
      console.error('Failed to load shopping items:', e);
      setItems([]);
    } finally {
      setLoadingItems(false);
    }
  };

  const loadCategories = async () => {
    if (!group?.id) return;
    setLoadingCategories(true);
    try {
      const data = await categoryApi.getGroupCategories(group.id, { page: 0, size: 100 });
      setCategories(data?.content ?? data?.items ?? []);
    } catch (e) {
      console.error('Failed to load categories:', e);
      setCategories([]);
    } finally {
      setLoadingCategories(false);
    }
  };

  const submitItem = async () => {
    setItemFormError('');
    if (!itemForm.name.trim()) { setItemFormError('Name required'); return; }
    if (!itemForm.quantity || Number(itemForm.quantity) <= 0) { setItemFormError('Quantity must be positive'); return; }
    
    setSubmittingItem(true);
    try {
      const payload = {
        name: itemForm.name.trim(),
        quantity: Number(itemForm.quantity),
        categoryId: itemForm.categoryId || null
      };
      await shoppingApi.create(group.id, payload);
      setItemForm({ name: '', quantity: '', categoryId: '' });
      await loadItems();
    } catch (e) {
      setItemFormError(e.message || 'Failed to create item');
    } finally {
      setSubmittingItem(false);
    }
  };

  const submitCategory = async () => {
    setCategoryFormError('');
    if (!categoryForm.name.trim()) { setCategoryFormError('Name required'); return; }
    
    setSubmittingCategory(true);
    try {
      const payload = {
        name: categoryForm.name.trim(),
        color: categoryForm.color
      };
      await categoryApi.create(group.id, payload);
      setCategoryForm({ name: '', color: '#3B82F6' });
      await loadCategories();
    } catch (e) {
      setCategoryFormError(e.message || 'Failed to create category');
    } finally {
      setSubmittingCategory(false);
    }
  };

  const handleDeleteItem = async (itemId) => {
    if (!confirm('Delete this item?')) return;
    try {
      await shoppingApi.delete(group.id, itemId);
      await loadItems();
    } catch (e) {
      console.error('Delete failed', e);
      alert(e.message || 'Delete failed');
    }
  };

  const handleDeleteCategory = async (categoryId) => {
    if (!confirm('Delete this category? Items with this category will be uncategorized.')) return;
    try {
      await categoryApi.delete(group.id, categoryId);
      await loadCategories();
      await loadItems();
    } catch (e) {
      console.error('Delete failed', e);
      alert(e.message || 'Delete failed');
    }
  };

  const handleUpdateItemQuantity = async (itemId, newQuantity) => {
    try {
      await shoppingApi.updateQuantity(group.id, itemId, { quantity: Number(newQuantity) });
      await loadItems();
      setEditingItem(null);
    } catch (e) {
      console.error('Update failed', e);
      alert(e.message || 'Update failed');
    }
  };

  const handleUpdateItemName = async (itemId, newName) => {
    if (!newName.trim()) return;
    try {
      await shoppingApi.updateName(group.id, itemId, { name: newName.trim() });
      await loadItems();
      setEditingItem(null);
    } catch (e) {
      console.error('Update failed', e);
      alert(e.message || 'Update failed');
    }
  };

  const handleUpdateItemCategory = async (itemId, categoryId) => {
    try {
      await shoppingApi.updateCategory(group.id, itemId, { categoryId: categoryId || null });
      await loadItems();
    } catch (e) {
      console.error('Update failed', e);
      alert(e.message || 'Update failed');
    }
  };

  const handleUpdateCategoryName = async (categoryId, newName) => {
    if (!newName.trim()) return;
    try {
      await categoryApi.updateName(group.id, categoryId, { name: newName.trim() });
      await loadCategories();
      setEditingCategory(null);
    } catch (e) {
      console.error('Update failed', e);
      alert(e.message || 'Update failed');
    }
  };

  const handleUpdateCategoryColor = async (categoryId, newColor) => {
    try {
      await categoryApi.updateColor(group.id, categoryId, { color: newColor });
      await loadCategories();
    } catch (e) {
      console.error('Update failed', e);
      alert(e.message || 'Update failed');
    }
  };

  const groupedItems = items.reduce((acc, item) => {
    const categoryName = item.category?.name || 'Uncategorized';
    if (!acc[categoryName]) {
      acc[categoryName] = {
        category: item.category,
        items: []
      };
    }
    acc[categoryName].items.push(item);
    return acc;
  }, {});

  return (
    <div className="bg-white rounded-lg shadow-md p-8 max-w-5xl mx-auto mt-4">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Shopping List</h2>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white border rounded-md p-4">
            <h3 className="text-lg font-semibold mb-3 text-gray-800">Add Item</h3>

            {!group && loadingGroup && <div className="text-sm text-gray-600">Loading group...</div>}
            {!group && !loadingGroup && <div className="text-sm text-red-600">You are not in a group</div>}

            {group && (
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                  <input type="text" value={itemForm.name} onChange={e => setItemForm({...itemForm, name: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Quantity</label>
                  <input type="number" step="1" min="1" value={itemForm.quantity} onChange={e => setItemForm({...itemForm, quantity: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
                  <select value={itemForm.categoryId} onChange={e => setItemForm({...itemForm, categoryId: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">None</option>
                    {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>

                {itemFormError && <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">{itemFormError}</div>}

                <button onClick={submitItem} disabled={submittingItem}
                  className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors font-medium">
                  {submittingItem ? 'Adding...' : 'Add Item'}
                </button>
              </div>
            )}
          </div>

          <div className="bg-white border rounded-md p-4">
            <h3 className="text-lg font-semibold mb-3 text-gray-800">Shopping Items</h3>
            {loadingItems && <div className="text-sm text-gray-600">Loading...</div>}
            {!loadingItems && items.length === 0 && <div className="text-sm text-gray-600">No items yet</div>}

            <div className="space-y-4">
              {Object.entries(groupedItems).map(([categoryName, { category, items: categoryItems }]) => (
                <div key={categoryName} className="border rounded-md p-3">
                  <div className="flex items-center gap-2 mb-3">
                    {category && (
                      <div className="w-4 h-4 rounded" style={{ backgroundColor: category.color }}></div>
                    )}
                    <h4 className="font-semibold text-gray-800">{categoryName}</h4>
                    <span className="text-xs text-gray-500">({categoryItems.length})</span>
                  </div>
                  <ul className="space-y-2">
                    {categoryItems.map(item => (
                      <li key={item.id} className="border rounded p-3 bg-gray-50">
                        <div className="flex justify-between items-start">
                          <div className="flex-1">
                            {editingItem === item.id ? (
                              <input
                                type="text"
                                defaultValue={item.name}
                                onBlur={(e) => handleUpdateItemName(item.id, e.target.value)}
                                onKeyPress={(e) => e.key === 'Enter' && handleUpdateItemName(item.id, e.target.value)}
                                className="text-sm font-medium px-2 py-1 border border-blue-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                                autoFocus
                              />
                            ) : (
                              <div className="text-sm font-medium text-gray-800 cursor-pointer" onClick={() => setEditingItem(item.id)}>
                                {item.name}
                              </div>
                            )}
                            <div className="flex items-center gap-4 mt-2">
                              <div className="flex items-center gap-2">
                                <span className="text-xs text-gray-600">Qty:</span>
                                <input
                                  type="number"
                                  value={item.quantity}
                                  onChange={(e) => handleUpdateItemQuantity(item.id, e.target.value)}
                                  className="w-16 text-sm px-2 py-1 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                              </div>
                              <div className="flex items-center gap-2">
                                <span className="text-xs text-gray-600">Category:</span>
                                <select
                                  value={item.category?.id || ''}
                                  onChange={(e) => handleUpdateItemCategory(item.id, e.target.value)}
                                  className="text-xs px-2 py-1 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500">
                                  <option value="">None</option>
                                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </select>
                              </div>
                            </div>
                          </div>
                          <button onClick={() => handleDeleteItem(item.id)}
                            className="text-sm text-red-600 hover:text-red-700 ml-4">Delete</button>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}
            </div>
          </div>
        </div>

        <aside className="space-y-6">
          <div className="bg-white border rounded-md p-4">
            <h3 className="text-lg font-semibold mb-3 text-gray-800">Create Category</h3>

            {group && (
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                  <input type="text" value={categoryForm.name} onChange={e => setCategoryForm({...categoryForm, name: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Color</label>
                  <div className="flex items-center gap-2">
                    <input type="color" value={categoryForm.color} onChange={e => setCategoryForm({...categoryForm, color: e.target.value})}
                      className="w-12 h-10 border border-gray-300 rounded cursor-pointer" />
                    <input type="text" value={categoryForm.color} onChange={e => setCategoryForm({...categoryForm, color: e.target.value})}
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                  </div>
                </div>

                {categoryFormError && <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">{categoryFormError}</div>}

                <button onClick={submitCategory} disabled={submittingCategory}
                  className="w-full bg-green-600 text-white py-2 px-4 rounded-md hover:bg-green-700 transition-colors font-medium">
                  {submittingCategory ? 'Creating...' : 'Create Category'}
                </button>
              </div>
            )}
          </div>

          <div className="bg-white border rounded-md p-4">
            <h3 className="text-lg font-semibold mb-3 text-gray-800">Categories</h3>

            {loadingCategories && <div className="text-sm text-gray-600">Loading...</div>}
            {!loadingCategories && categories.length === 0 && <div className="text-sm text-gray-600">No categories yet</div>}

            <ul className="space-y-2">
              {categories.map(cat => (
                <li key={cat.id} className="border rounded p-3 bg-gray-50">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2 flex-1">
                      <input
                        type="color"
                        value={cat.color}
                        onChange={(e) => handleUpdateCategoryColor(cat.id, e.target.value)}
                        className="w-8 h-8 border border-gray-300 rounded cursor-pointer"
                      />
                      {editingCategory === cat.id ? (
                        <input
                          type="text"
                          defaultValue={cat.name}
                          onBlur={(e) => handleUpdateCategoryName(cat.id, e.target.value)}
                          onKeyPress={(e) => e.key === 'Enter' && handleUpdateCategoryName(cat.id, e.target.value)}
                          className="flex-1 text-sm font-medium px-2 py-1 border border-blue-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                          autoFocus
                        />
                      ) : (
                        <div className="flex-1 text-sm font-medium text-gray-800 cursor-pointer" onClick={() => setEditingCategory(cat.id)}>
                          {cat.name}
                        </div>
                      )}
                    </div>
                    <button onClick={() => handleDeleteCategory(cat.id)}
                      className="text-sm text-red-600 hover:text-red-700 ml-2">Delete</button>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </aside>
      </div>
    </div>
  );
}