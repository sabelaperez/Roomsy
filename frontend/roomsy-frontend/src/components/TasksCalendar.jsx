import React, { useContext, useEffect, useMemo, useState } from 'react';
import Calendar from './Calendar';
import { AuthContext } from '../context/AuthContext';
import { groupApi, tasksApi } from '../api';
import ConfirmModal from './ConfirmModal';

export default function TasksCalendar() {
  const { user } = useContext(AuthContext);
  const [group, setGroup] = useState(null);
  const [loadingGroup, setLoadingGroup] = useState(true);

  const [groupMembers, setGroupMembers] = useState([]);
  const [loadingMembers, setLoadingMembers] = useState(false);

  const [tasksPage, setTasksPage] = useState(null);
  const [loadingTasks, setLoadingTasks] = useState(false);
  const [error, setError] = useState('');

  const [selectedTask, setSelectedTask] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [detailsError, setDetailsError] = useState('');

  const [editingTitle, setEditingTitle] = useState(false);
  const [titleDraft, setTitleDraft] = useState('');
  const [editingDate, setEditingDate] = useState(false);
  const [dateDraft, setDateDraft] = useState('');

  const [createModalAlert, setCreateModalAlert] = useState({ visible: false, text: "" });
  const [detailModalAlert, setDetailModalAlert] = useState({ visible: false, text: "" });

  const [confirmModal, setConfirmModal] = useState({
    isOpen: false,
    title: '',
    message: '',
    onConfirm: null,
    confirmText: 'Confirm',
    variant: 'danger'
  });

  const [errorMessage, setErrorMessage] = useState('');

  const toInputDateTime = (iso) => {
    if (!iso) return '';
    const d = new Date(iso);
    if (isNaN(d)) return '';
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  };

  const parseLocalDateTime = (s) => {
    if (!s) return null;
    const m = String(s).match(/^(\d{4})-(\d{2})-(\d{2})T(.+)$/);
    if (m) return new Date(`${m[1]}-${m[2]}-${m[3]}T${m[4]}`);
    const d = new Date(s);
    return isNaN(d) ? null : d;
  };

  useEffect(() => {
    if (!user) return;
    let mounted = true;
    setLoadingGroup(true);
    groupApi.get(user.userId)
      .then(g => { if (mounted) setGroup(g); })
      .catch(() => { if (mounted) setGroup(null); })
      .finally(() => { if (mounted) setLoadingGroup(false); });
    return () => { mounted = false; };
  }, [user]);

  const loadTasks = async () => {
    if (!group?.id) return;
    setLoadingTasks(true); setError('');
    try {
      const data = await tasksApi.getGroupTasks(group.id, { page: 0, size: 500, sortBy: 'date', sortDirection: 'asc' });
      setTasksPage(data);
    } catch (e) {
      setError(e.message || 'Failed to load tasks');
      setTasksPage(null);
    } finally { setLoadingTasks(false); }
  };

  useEffect(() => { loadTasks(); loadMembers(); }, [group?.id]);

  const tasks = tasksPage?.content ?? [];

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
    setNewTask((f) => {
      const list = Array.isArray(f.assignedToIds) ? f.assignedToIds : [];
      const exists = list.includes(idStr);
      const next = exists ? list.filter(x => x !== idStr) : [...list, idStr];
      return { ...f, assignedToIds: next };
    });
  };

  const events = useMemo(() => tasks.map(t => {
    const dateIso = t.date ?? t.taskDate ?? t.dueDate ?? t.createdAt;
    return {
      id: t.id,
      title: t.title ?? 'Task',
      date: dateIso,
      assignedTo: t.assignedTo ?? [],
      completed: !!t.completed
    };
  }), [tasks]);

  const handleEventClick = (ev) => {
    const found = ev?.id ? tasks.find(t => String(t.id) === String(ev.id)) : null;
    setSelectedTask(found ?? null);

    if (ev?.id && group?.id) {
      setDetailsLoading(true); setDetailsError('');
      tasksApi.getTask(group.id, ev.id)
        .then(d => {
          setSelectedTask(d);
        })
        .catch(e => setDetailsError(e.message || 'Failed to load task'))
        .finally(() => setDetailsLoading(false));
    }
  };

  const startEditTitle = () => {
    setTitleDraft(selectedTask?.title ?? selectedTask?.name ?? '');
    setEditingTitle(true);
  };

  const saveTitle = async () => {
    if (!group?.id || !selectedTask?.id) { setEditingTitle(false); return; }
    const newTitle = (titleDraft || '').trim();
    if (!newTitle) { setEditingTitle(false); return; }
    const prev = selectedTask;
    setSelectedTask(prev => ({ ...prev, title: newTitle }));
    setEditingTitle(false);
    try {
      await tasksApi.changeTitle(group.id, selectedTask.id, newTitle);
      await loadTasks();
    } catch (e) {
      setSelectedTask(prev);
      setErrorMessage(e.message || 'Failed to update title');
    }
  };

  const startEditDate = () => {
    setDateDraft(toInputDateTime(selectedTask?.date ?? ''));
    setEditingDate(true);
  };

  const saveDate = async () => {
    if (!group?.id || !selectedTask?.id) { setEditingDate(false); return; }
    if (!dateDraft) { setEditingDate(false); return; }
    const prev = selectedTask;
    const payloadDate = dateDraft.length === 16 ? `${dateDraft}:00` : dateDraft;
    setSelectedTask(prev => ({ ...prev, date: payloadDate }));
    setEditingDate(false);
    try {
      await tasksApi.changeDate(group.id, selectedTask.id, dateDraft);
      await loadTasks();
    } catch (e) {
      setSelectedTask(prev);
      setErrorMessage(e.message || 'Failed to update date');
    }
  };

  const toggleAssignedForSelectedTask = async (userId) => {
    if (!group?.id || !selectedTask?.id) return;
    setDetailModalAlert({ visible: false, text: "" });
    const currentIds = Array.isArray(selectedTask.assignedTo)
      ? selectedTask.assignedTo.map(a => String(a.id ?? a.userId ?? a))
      : [];
    const idStr = String(userId);
    const next = currentIds.includes(idStr) ? currentIds.filter(x => x !== idStr) : [...currentIds, idStr];

    if (next.length === 0) {
      setDetailModalAlert(true, "At least 1 assignee is required");
      return;
    }

    const prev = selectedTask;
    setSelectedTask(prev => ({ ...prev, assignedTo: next.map(id => ({ id })) }));
    try {
      await tasksApi.reassignTask(group.id, selectedTask.id, next);
      await loadTasks();
    } catch (e) {
      setSelectedTask(prev);
      setErrorMessage(e.message || 'Failed to update assignees');
    }
  };

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newTask, setNewTask] = useState({ 
    title: '',
    date: '',
    assignedToIds: [],
    completed: false,
  });
  const [creating, setCreating] = useState(false);

  const submitCreate = async () => {
    if (!group?.id) return;

    if(newTask.title.trim() === '') {
      setCreateModalAlert({ visible: true, text: "Title is required" });
      return;
    }
    
    if(newTask.date === '') {
      setCreateModalAlert({ visible: true, text: "Date is required" });
      return;
    }
    if(newTask.assignedToIds.length === 0) {
      setCreateModalAlert({ visible: true, text: "At least 1 assignee is required" });
      return;
    }

    setCreating(true);
    try {
      const payload = {
        title: newTask.title,
        date: newTask.date,
        assignedToIds: newTask.assignedToIds,
        completed: false,
      };
      await tasksApi.createTask(group.id, payload);
      setShowCreateModal(false);
      setNewTask({ title: '', date: '', assignedToIds: [], completed: false });
      await loadTasks();
    } catch (e) {
      setErrorMessage(e.message || 'Create failed');
    } finally { setCreating(false); }
  };

  const doDelete = (taskId) => {
    setConfirmModal({
      isOpen: true,
      title: 'Delete Task',
      message: 'Are you sure you want to delete this task? This action cannot be undone.',
      confirmText: 'Delete',
      variant: 'danger',
      onConfirm: async () => {
        if (!group?.id) return;
        try {
          await tasksApi.deleteTask(group.id, taskId);
          await loadTasks();
          setSelectedTask(null);
          setConfirmModal({ ...confirmModal, isOpen: false });
        } catch (e) {
          setErrorMessage(e.message || 'Delete failed');
          setConfirmModal({ ...confirmModal, isOpen: false });
        }
      }
    });
  };

  const toggleSelectedTaskCompleted = async (checked) => {
    setCreateModalAlert({ visible: false, text: "" });
    if(!group?.id || !selectedTask?.id) return;
    try {
      setSelectedTask(prev => ({ ...prev, completed: !!checked }));
      await tasksApi.setCompleted(group.id, selectedTask.id, !!checked);
      await loadTasks();
    } catch (e) {
      setSelectedTask(prev => ({ ...prev, completed: !checked }));
      setErrorMessage(e.message || 'Operation failed');
    }
  };

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
        <h2 className="text-2xl font-bold text-gray-800">Tasks Calendar</h2>

        {loadingTasks && <div className="text-sm text-gray-600">Loading tasks...</div>}

        <div className="flex gap-2">
          <button onClick={() => setShowCreateModal(true)} className="bg-blue-600 text-white py-2 px-3 rounded-md hover:bg-blue-700 text-sm">+ Create Task</button>
        </div>
      </div>

      {errorMessage && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {errorMessage}
          <button onClick={() => setErrorMessage('')} className="ml-4 text-red-900 hover:text-red-700">✕</button>
        </div>
      )}

      <Calendar
        events={events}
        onEventsChange={() => {}}
        onDayOpen={null}
        onEventClick={handleEventClick}
      />

      {/* Task detail modal */}
      {selectedTask && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4" onClick={() => setSelectedTask(null)}>
          <div className="bg-white rounded-lg w-full max-w-2xl shadow-lg p-6" onClick={e => e.stopPropagation()}>
            {detailsLoading && <div className="mt-4 text-sm text-gray-600">Loading...</div>}
            {detailsError && <div className="mt-4 text-sm text-red-600">{detailsError}</div>}
            {detailModalAlert.visible && (
              <div className="mt-4 bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded text-sm">
                {detailModalAlert}
              </div>
            )}

            <div className="flex justify-between items-start">
              <div>
                {editingTitle ? (
                  <input
                    autoFocus
                    className="text-lg font-semibold px-2 py-1 border rounded"
                    value={titleDraft}
                    onChange={(e) => setTitleDraft(e.target.value)}
                    onBlur={saveTitle}
                    onKeyDown={(e) => { if (e.key === 'Enter') saveTitle(); if (e.key === 'Escape') setEditingTitle(false); }}
                  />
                ) : (
                  <h3 onClick={startEditTitle} className="text-lg font-semibold text-gray-800 cursor-pointer">{selectedTask.title ?? selectedTask.name}</h3>
                )}
                <div className="text-xs text-gray-500">Click to edit</div>
              </div>
              <button onClick={() => setSelectedTask(null)} className="text-gray-500">✕</button>
            </div>

            {!detailsLoading && selectedTask && (
              <div className="mt-4 space-y-3 text-sm text-gray-800">
                <div>
                  <div className="text-xs text-gray-500">Date (click to edit)</div>
                  {editingDate ? (
                    <input
                      type="datetime-local"
                      value={dateDraft}
                      onChange={(e) => setDateDraft(e.target.value)}
                      onBlur={saveDate}
                      onKeyDown={(e) => { if (e.key === 'Enter') saveDate(); if (e.key === 'Escape') setEditingDate(false); }}
                      className="px-3 py-2 border rounded"
                      autoFocus
                    />
                  ) : (
                    <div onClick={startEditDate} className="cursor-pointer">{parseLocalDateTime(selectedTask.date)?.toLocaleString() || '-'}</div>
                  )}
                </div>

                <div>
                  <div className="text-xs text-gray-500 mb-2">Assigned to (click to toggle)</div>
                  <div className="flex flex-wrap gap-2">
                    {groupMembers.map(m => {
                      const id = String(m.id);
                      const included = Array.isArray(selectedTask.assignedTo) && selectedTask.assignedTo.some(a => String(a.id ?? a.userId ?? a) === id);
                      return (
                        <button
                          key={id}
                          onClick={() => toggleAssignedForSelectedTask(id)}
                          className={`text-sm px-2 py-1 rounded border ${included ? 'bg-blue-600 text-white border-blue-600' : 'bg-gray-200 text-gray-800 border-gray-300'}`}
                        >
                          {m.fullName ?? m.username}
                        </button>
                      );
                    })}
                    {(groupMembers.length === 0) && <div className="text-xs text-gray-600">No members</div>}
                  </div>
                </div>

                <div className="flex justify-between items-start">
                  <label className="inline-flex items-center gap-2 mt-1">
                    <input
                      type="checkbox"
                      checked={!!selectedTask.completed}
                      onChange={(e) => toggleSelectedTaskCompleted(e.target.checked)}
                    />
                    <span className="text-sm">{selectedTask.completed ? 'Completed' : 'Not completed'}</span>
                  </label>

                  <button onClick={() => doDelete(selectedTask.id)} className="px-3 py-2 bg-red-600 text-white rounded hover:bg-red-700 text-sm">Delete</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Create task modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4" onClick={() => setShowCreateModal(false)}>
          <div className="bg-white rounded-lg w-full max-w-lg shadow-lg p-6" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold text-gray-800">Create Task</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-gray-500">✕</button>
            </div>

            {!group && loadingGroup && <div className="text-sm text-gray-600">Loading group...</div>}
            {!group && !loadingGroup && <div className="text-sm text-red-600">You are not in a group</div>}
            {createModalAlert.visible && (
              <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded text-sm">
                {createModalAlert.text}
              </div>
            )}

            <div className="space-y-3">
              <div>
                <label className="block text-sm text-gray-700 mb-1">Title</label>
                <input value={newTask.title} onChange={e => setNewTask({...newTask, title: e.target.value})} className="w-full px-3 py-2 border rounded" />
              </div>
              <div>
                <label className="block text-sm text-gray-700 mb-1">Date</label>
                <input type="datetime-local" value={newTask.date} onChange={e => setNewTask({...newTask, date: e.target.value})} className="w-full px-3 py-2 border rounded" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Users involved</label>
                <div className="flex flex-wrap gap-2">
                  {loadingMembers && <div className="text-sm text-gray-600">Loading members...</div>}
                  {!loadingMembers && groupMembers.map(m => {
                    const id = String(m.id);
                    const included = Array.isArray(newTask.assignedToIds) && newTask.assignedToIds.some(x => String(x) === id);
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

              <div className="flex gap-2">
                <button onClick={submitCreate} disabled={creating} className="flex-1 bg-blue-600 text-white py-2 px-3 rounded-md hover:bg-blue-700 text-sm">
                  {creating ? 'Creating...' : 'Create Task'}
                </button>
                <button onClick={() => setShowCreateModal(false)} className="px-4 py-2 bg-gray-100 rounded-md hover:bg-gray-200">Cancel</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
