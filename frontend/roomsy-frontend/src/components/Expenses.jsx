import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { expenseApi, groupApi } from '../api';
import ConfirmModal from './ConfirmModal';

const EXPENSE_TYPES = ['GROCERIES', 'RENT', 'SUPPLIES', 'ENTERTAINMENT', 'OTHER'];

export default function Expenses() {
  const { user } = useContext(AuthContext);
  const [group, setGroup] = useState(null);
  const [loadingGroup, setLoadingGroup] = useState(true);

  const [groupMembers, setGroupMembers] = useState([]);
  const [loadingMembers, setLoadingMembers] = useState(false);

  const [expensesPage, setExpensesPage] = useState(null);
  const [loadingExpenses, setLoadingExpenses] = useState(false);
  const [sharedPage, setSharedPage] = useState(null);
  const [loadingShared, setLoadingShared] = useState(false);

  const [submitting, setSubmitting] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);

  const [form, setForm] = useState({
    name: '',
    expenseType: EXPENSE_TYPES[0],
    price: '',
    expenseDate: '',
    usersInvolvedIds: []
  });
  const [formError, setFormError] = useState('');

  const [confirmModal, setConfirmModal] = useState({
    isOpen: false,
    title: '',
    message: '',
    onConfirm: null,
    confirmText: 'Confirm',
    variant: 'danger'
  });

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
    loadExpenses();
    loadShared();
    loadMembers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [group?.id]);

  const loadExpenses = async () => {
    if (!group?.id) return;
    setLoadingExpenses(true);
    try {
      const data = await expenseApi.getExpenseItems(group.id, { page: 0, size: 20 });
      setExpensesPage(data);
    } catch (e) {
      console.error('Failed to load expenses:', e);
      setExpensesPage(null);
    } finally {
      setLoadingExpenses(false);
    }
  };

  const loadShared = async () => {
    if (!group?.id) return;
    setLoadingShared(true);
    try {
      const data = await expenseApi.getSharedExpenses(group.id, { page: 0, size: 20 });
      setSharedPage(data);
    } catch (e) {
      console.error('Failed to load shared expenses:', e);
      setSharedPage(null);
    } finally {
      setLoadingShared(false);
    }
  };

  const loadMembers = async () => {
    if (!group?.id) {
      setGroupMembers([]);
      return;
    }
    setLoadingMembers(true);
    try {
      const members = await groupApi.getMembers(group.id);
      setGroupMembers(Array.isArray(members) ? members.map(m => ({
        id: String(m.id || m.userId),
        username: m.username,
        fullName: m.fullName,
        email: m.email,
      })) : []);
    } catch (e) {
      console.error('Failed to load group members:', e);
      setGroupMembers([]);
    } finally {
      setLoadingMembers(false);
    }
  };

  const toggleUserInvolved = (id) => {
    const idStr = String(id);
    setForm((f) => {
      const exists = f.usersInvolvedIds.includes(idStr);
      return { ...f, usersInvolvedIds: exists ? f.usersInvolvedIds.filter(x => x !== idStr) : [...f.usersInvolvedIds, idStr] };
    });
  };

  const submitExpense = async () => {
    setFormError('');
    if (!form.name.trim()) { setFormError('Name required'); return; }
    if (!form.price || Number(form.price) <= 0) { setFormError('Price must be positive'); return; }
    if (form.usersInvolvedIds.length === 0) { setFormError('Select at least one user involved'); return; }
    setSubmitting(true);
    try {
      const payload = {
        ownerId: user.userId,
        name: form.name.trim(),
        expenseType: form.expenseType,
        usersInvolvedIds: form.usersInvolvedIds,
        price: Number(form.price),
        expenseDate: form.expenseDate ? new Date(form.expenseDate) : new Date()
      };
      await expenseApi.create(group.id, payload);
      setForm({ name: '', expenseType: EXPENSE_TYPES[0], price: '', expenseDate: '', usersInvolvedIds: [] });
      await loadExpenses();
      await loadShared();
      setShowCreateModal(false);
    } catch (e) {
      setFormError(e.message || 'Failed to create expense');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteExpense = (expenseId) => {
    setConfirmModal({
      isOpen: true,
      title: 'Delete Expense',
      message: 'Are you sure you want to delete this expense? This action cannot be undone.',
      confirmText: 'Delete',
      variant: 'danger',
      onConfirm: async () => {
        try {
          await expenseApi.delete(group.id, expenseId);
          await loadExpenses();
          await loadShared();
          setConfirmModal({ ...confirmModal, isOpen: false });
        } catch (e) {
          console.error('Delete failed', e);
          setFormError(e.message || 'Delete failed');
          setConfirmModal({ ...confirmModal, isOpen: false });
        }
      }
    });
  };

  const handlePayShared = (sharedId) => {
    setConfirmModal({
      isOpen: true,
      title: 'Mark as Paid',
      message: 'Are you sure you want to mark this shared expense as paid?',
      confirmText: 'Mark Paid',
      variant: 'success',
      onConfirm: async () => {
        try {
          await expenseApi.pay(group.id, sharedId);
          await loadShared();
          setConfirmModal({ ...confirmModal, isOpen: false });
        } catch (e) {
          console.error('Pay failed', e);
          setFormError(e.message || 'Operation failed');
          setConfirmModal({ ...confirmModal, isOpen: false });
        }
      }
    });
  };

  const closeCreateModal = () => {
    setShowCreateModal(false);
    setForm({ name: '', expenseType: EXPENSE_TYPES[0], price: '', expenseDate: '', usersInvolvedIds: [] });
    setFormError('');
  };

  const expenses = expensesPage?.content ?? expensesPage?.items ?? [];
  const shared = sharedPage?.content ?? sharedPage?.items ?? [];

  return (
    <div className="bg-white rounded-lg shadow-md p-8 max-w-5xl mx-auto mt-4">
      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title={confirmModal.title}
        message={confirmModal.message}
        confirmText={confirmModal.confirmText}
        confirmVariant={confirmModal.variant}
        onConfirm={confirmModal.onConfirm}
        onCancel={() => setConfirmModal({ ...confirmModal, isOpen: false })}
      />

      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Group Expenses</h2>
        <button
          onClick={() => setShowCreateModal(true)}
          className="ml-4 bg-blue-600 text-white py-2 px-3 rounded-md hover:bg-blue-700 text-sm"
        >
          + Create Expense
        </button>
      </div>

      <div className="bg-white shadow-md rounded-md p-4 mb-6">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-lg font-semibold text-gray-800">Balances</h3>
          {loadingShared && <div className="text-sm text-gray-600">Loading...</div>}
        </div>

        {!loadingShared && shared.length === 0 && (
          <div className="text-sm text-gray-600 mb-3">No debts</div>
        )}

        <div className="flex gap-4 overflow-x-auto py-2">
          {shared.map(s => (
            <div key={s.id} className="min-w-[200px] flex-shrink-0 border rounded p-4 bg-gray-50">
              <div className="text-sm text-gray-800">
                <span className="font-medium">{s.notPaid?.fullName}</span> owes to <span className="font-medium">{s.payer?.fullName}</span>
              </div>
              <div className="text-m text-gray-700 mt-1 font-semibold">{s.quantity?.toFixed(2)} €</div>

              <div className="mt-3">
                {String(s.payer?.id) === String(user?.userId) || String(s.notPaid?.id) === String(user?.userId) ? (
                  <button onClick={() => handlePayShared(s.id)}
                    className="w-full bg-green-600 text-white py-2 px-3 rounded-md hover:bg-green-700 transition-colors text-sm">
                    Mark Paid
                  </button>
                ) : (
                  <button
                    className="w-full bg-gray-300 text-white py-2 px-3 rounded-md transition-colors text-sm">
                    Not involved
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
      
      <div className="space-y-6">
        <div className="bg-white shadow-md rounded-md p-4">
          <h3 className="text-lg font-semibold mb-3 text-gray-800">Expenses</h3>
          {loadingExpenses && <div className="text-sm text-gray-600">Loading...</div>}
          {!loadingExpenses && expenses.length === 0 && <div className="text-sm text-gray-600">No expenses yet</div>}

          <ul className="space-y-3">
            {expenses.map(e => (
              <li key={e.id} className="border rounded p-3 bg-gray-50">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="text-sm font-medium text-gray-800">{e.name}</div>
                    <div className="text-xs text-gray-600">By {e.ownerUsername} • {e.expenseType} • {new Date(e.expenseDate).toLocaleDateString()}</div>
                    <div className="text-sm text-gray-700 mt-2">Price: <span className="font-semibold">{e.price?.toFixed(2)} €</span></div>
                    <div className="text-xs text-gray-600 mt-1">Involved: {(e.usersInvolved || []).map(u => u.username ?? u.fullName).join(', ')}</div>
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <div className="text-xs text-gray-500">{new Date(e.createdAt).toLocaleString()}</div>
                    <div>
                      <button onClick={() => handleDeleteExpense(e.id)}
                        className="text-sm text-red-600 hover:text-red-700">Delete</button>
                    </div>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {/* Create Expense Modal */}
      {showCreateModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4"
          onClick={closeCreateModal}
          aria-modal="true"
          role="dialog"
        >
          <div
            className="bg-white rounded-lg w-full max-w-lg shadow-lg p-6"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold text-gray-800">Create Expense</h3>
              <button onClick={closeCreateModal} className="text-gray-500 hover:text-gray-700">✕</button>
            </div>

            {!group && loadingGroup && <div className="text-sm text-gray-600">Loading group...</div>}
            {!group && !loadingGroup && <div className="text-sm text-red-600">You are not in a group</div>}

            {group && (
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                  <input type="text" value={form.name} onChange={e => setForm({...form, name: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
                  <select value={form.expenseType} onChange={e => setForm({...form, expenseType: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    {EXPENSE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Price</label>
                  <input type="number" step="0.01" value={form.price} onChange={e => setForm({...form, price: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Date</label>
                  <input type="date" value={form.expenseDate} onChange={e => setForm({...form, expenseDate: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Users involved</label>
                  <div className="flex flex-wrap gap-2">
                    {loadingMembers && <div className="text-sm text-gray-600">Loading members...</div>}
                    {!loadingMembers && groupMembers.map(m => {
                      const id = String(m.id);
                      const included = Array.isArray(form.usersInvolvedIds) && form.usersInvolvedIds.some(x => String(x) === id);
                      return (
                        <button
                          key={id}
                          onClick={() => toggleUserInvolved(id)}
                          className={`text-sm px-2 py-1 rounded border ${included ? 'bg-blue-600 text-white border-blue-600' : 'bg-gray-200 text-gray-800 border-gray-300'}`}
                        >
                          {m.fullName ?? m.username}
                        </button>
                      );
                    })}
                    {(groupMembers.length === 0) && <div className="text-xs text-gray-600">No members</div>}
                  </div>
                </div>

                {formError && <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">{formError}</div>}

                <div className="flex gap-2">
                  <button onClick={submitExpense} disabled={submitting}
                    className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors font-medium">
                    {submitting ? 'Creating...' : 'Create Expense'}
                  </button>
                  <button onClick={closeCreateModal} className="px-4 py-2 bg-gray-100 rounded-md hover:bg-gray-200">Cancel</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
