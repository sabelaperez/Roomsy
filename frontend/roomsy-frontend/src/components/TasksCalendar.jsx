import React, { useContext, useEffect, useMemo, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { groupApi, tasksApi } from '../api';

function toYMD(d) {
  const dt = new Date(d);
  const y = dt.getFullYear();
  const m = String(dt.getMonth() + 1).padStart(2, '0');
  const day = String(dt.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function parseLocalDateTime(s) {
  if (!s) return null;
  // handle "2024-06-15T14:30:00" (no timezone)
  const m = s.match(/^(\d{4})-(\d{2})-(\d{2})T(.+)$/);
  if (m) return new Date(`${m[1]}-${m[2]}-${m[3]}T${m[4]}`);
  const d = new Date(s);
  return isNaN(d) ? null : d;
}

export default function TasksCalendar() {
  const { user } = useContext(AuthContext);
  const [group, setGroup] = useState(null);
  const [loadingGroup, setLoadingGroup] = useState(true);

  const [tasksPage, setTasksPage] = useState(null);
  const [loadingTasks, setLoadingTasks] = useState(false);
  const [error, setError] = useState('');

  const [currentMonth, setCurrentMonth] = useState(() => {
    const d = new Date();
    d.setDate(1);
    return d;
  });

  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedTaskId, setSelectedTaskId] = useState(null);
  const [selectedTask, setSelectedTask] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [detailsError, setDetailsError] = useState('');

  // load group
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

  // load tasks for group
  useEffect(() => {
    if (!group?.id) return;
    let mounted = true;
    setLoadingTasks(true);
    setError('');
    tasksApi.getGroupTasks(group.id, { page: 0, size: 500, sortBy: 'date', sortDirection: 'asc' })
      .then(data => { if (mounted) setTasksPage(data); })
      .catch(e => { if (mounted) { setError(e.message || 'Failed to load tasks'); setTasksPage(null); } })
      .finally(() => { if (mounted) setLoadingTasks(false); });
    return () => { mounted = false; };
  }, [group?.id]);

  // map tasks by date (Y-M-D)
  const tasksByDate = useMemo(() => {
    const out = {};
    const items = tasksPage?.content ?? tasksPage?.items ?? [];
    for (const t of items) {
      const d = parseLocalDateTime(t.date ?? t.taskDate ?? t.dueDate) || parseLocalDateTime(t.createdAt) || new Date();
      const key = toYMD(d);
      out[key] = out[key] || [];
      out[key].push(t);
    }
    return out;
  }, [tasksPage]);

  // calendar grid for currentMonth
  const calendar = useMemo(() => {
    const first = new Date(currentMonth);
    const year = first.getFullYear();
    const month = first.getMonth();
    const firstDayWeek = new Date(year, month, 1).getDay(); // 0 Sun .. 6 Sat
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const weeks = [];
    let week = new Array(firstDayWeek).fill(null);
    for (let d = 1; d <= daysInMonth; d++) {
      week.push(new Date(year, month, d));
      if (week.length === 7) { weeks.push(week); week = []; }
    }
    if (week.length) {
      while (week.length < 7) week.push(null);
      weeks.push(week);
    }
    return weeks;
  }, [currentMonth]);

  // Task details load
  useEffect(() => {
    if (!selectedTaskId || !group?.id) { setSelectedTask(null); setDetailsError(''); setDetailsLoading(false); return; }
    let mounted = true;
    setDetailsLoading(true);
    setDetailsError('');
    tasksApi.getTask(group.id, selectedTaskId)
      .then(data => { if (mounted) setSelectedTask(data); })
      .catch(e => { if (mounted) { setDetailsError(e.message || 'Failed to load task'); setSelectedTask(null); } })
      .finally(() => { if (mounted) setDetailsLoading(false); });
    return () => { mounted = false; };
  }, [selectedTaskId, group?.id]);

  // actions
  const toggleComplete = async (taskId, completed) => {
    if (!group?.id) return;
    try {
      await tasksApi.setCompleted(group.id, taskId, !!completed);
      // reload tasks and details
      const data = await tasksApi.getGroupTasks(group.id, { page: 0, size: 500 });
      setTasksPage(data);
      if (selectedTaskId) {
        const det = await tasksApi.getTask(group.id, selectedTaskId);
        setSelectedTask(det);
      }
    } catch (e) {
      alert(e.message || 'Operation failed');
    }
  };

  const deleteTask = async (taskId) => {
    if (!group?.id) return;
    if (!confirm('Delete this task?')) return;
    try {
      await tasksApi.deleteTask(group.id, taskId);
      const data = await tasksApi.getGroupTasks(group.id, { page: 0, size: 500 });
      setTasksPage(data);
      setSelectedTaskId(null);
      setSelectedTask(null);
    } catch (e) {
      alert(e.message || 'Delete failed');
    }
  };

  const changeTitle = async (taskId, title) => {
    if (!group?.id) return;
    try {
      await tasksApi.changeTitle(group.id, taskId, title);
      const data = await tasksApi.getGroupTasks(group.id, { page: 0, size: 500 });
      setTasksPage(data);
      if (selectedTaskId) {
        const det = await tasksApi.getTask(group.id, selectedTaskId);
        setSelectedTask(det);
      }
    } catch (e) {
      alert(e.message || 'Update failed');
    }
  };

  const changeDate = async (taskId, newDate) => {
    if (!group?.id) return;
    try {
      await tasksApi.changeDate(group.id, taskId, newDate);
      const data = await tasksApi.getGroupTasks(group.id, { page: 0, size: 500 });
      setTasksPage(data);
      if (selectedTaskId) {
        const det = await tasksApi.getTask(group.id, selectedTaskId);
        setSelectedTask(det);
      }
    } catch (e) {
      alert(e.message || 'Update failed');
    }
  };

  const reassign = async (taskId, assignedToIds) => {
    if (!group?.id) return;
    try {
      await tasksApi.reassignTask(group.id, taskId, assignedToIds);
      const data = await tasksApi.getGroupTasks(group.id, { page: 0, size: 500 });
      setTasksPage(data);
      if (selectedTaskId) {
        const det = await tasksApi.getTask(group.id, selectedTaskId);
        setSelectedTask(det);
      }
    } catch (e) {
      alert(e.message || 'Update failed');
    }
  };

  // simple UI helpers
  const prevMonth = () => {
    const d = new Date(currentMonth);
    d.setMonth(d.getMonth() - 1);
    setCurrentMonth(d);
  };
  const nextMonth = () => {
    const d = new Date(currentMonth);
    d.setMonth(d.getMonth() + 1);
    setCurrentMonth(d);
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 max-w-5xl mx-auto mt-4">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-bold text-gray-800">Tasks Calendar</h2>
        <div className="flex items-center gap-2">
          <button onClick={prevMonth} className="px-3 py-1 bg-gray-100 rounded">Prev</button>
          <div className="text-sm font-medium">{currentMonth.toLocaleString(undefined, { month: 'long', year: 'numeric' })}</div>
          <button onClick={nextMonth} className="px-3 py-1 bg-gray-100 rounded">Next</button>
        </div>
      </div>

      {loadingGroup && <div className="text-sm text-gray-600 mb-4">Loading group...</div>}
      {error && <div className="text-sm text-red-600 mb-4">{error}</div>}

      <div className="grid grid-cols-7 gap-1 text-sm">
        {['Sun','Mon','Tue','Wed','Thu','Fri','Sat'].map(d => (
          <div key={d} className="text-center font-medium py-1">{d}</div>
        ))}
      </div>

      <div className="grid grid-cols-7 gap-1 mt-2">
        {calendar.map((week, wi) => (
          <React.Fragment key={wi}>
            {week.map((day, di) => {
              if (!day) return <div key={di} className="min-h-[80px] border p-2 bg-gray-50"></div>;
              const ymd = toYMD(day);
              const tasks = tasksByDate[ymd] || [];
              const isSelected = selectedDate === ymd;
              return (
                <div key={di} className={`min-h-[80px] border p-2 ${isSelected ? 'ring-2 ring-indigo-200' : 'bg-white'}`}>
                  <div className="flex justify-between items-start">
                    <div className="text-xs text-gray-500">{day.getDate()}</div>
                    <div className="text-xs text-gray-400">{tasks.length ? `${tasks.length}` : ''}</div>
                  </div>

                  <div className="mt-2 space-y-1">
                    {tasks.slice(0,3).map(t => (
                      <div key={t.id}
                        className="text-xs p-1 rounded cursor-pointer hover:bg-gray-100"
                        onClick={() => { setSelectedDate(ymd); setSelectedTaskId(t.id); }}>
                        {t.title ?? t.name ?? 'Task'}
                      </div>
                    ))}
                    {tasks.length > 3 && (
                      <div className="text-xs text-gray-400">+{tasks.length - 3} more</div>
                    )}
                  </div>

                  <div className="mt-2">
                    <button className="text-xs text-indigo-600" onClick={() => setSelectedDate(ymd)}>Open</button>
                  </div>
                </div>
              );
            })}
          </React.Fragment>
        ))}
      </div>

      {/* Day panel */}
      {selectedDate && (
        <div className="mt-4 bg-gray-50 border rounded p-4">
          <div className="flex justify-between items-center">
            <div>
              <h3 className="font-semibold text-gray-800">Tasks on {selectedDate}</h3>
              <div className="text-xs text-gray-600">{(tasksByDate[selectedDate] || []).length} task(s)</div>
            </div>
            <button onClick={() => { setSelectedDate(null); setSelectedTaskId(null); }} className="text-sm text-gray-600">Close</button>
          </div>

          <div className="mt-3 space-y-2">
            {(tasksByDate[selectedDate] || []).map(t => (
              <div key={t.id} className="border rounded p-3 bg-white flex justify-between items-start">
                <div>
                  <div className="text-sm font-medium text-gray-800">{t.title ?? t.name}</div>
                  <div className="text-xs text-gray-600">{t.description ?? ''}</div>
                  <div className="text-xs text-gray-500 mt-1">Assigned: {(t.assignedTo || []).map(a => a.username ?? a.fullName).join(', ')}</div>
                </div>
                <div className="flex flex-col items-end gap-2">
                  <div className="text-xs text-gray-500">{parseLocalDateTime(t.date ?? t.createdAt)?.toLocaleString()}</div>
                  <div>
                    <button
                      onClick={() => { setSelectedTaskId(t.id); }}
                      className="text-sm text-indigo-600">View</button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Task detail modal */}
      {selectedTaskId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4"
             onClick={() => { setSelectedTaskId(null); setSelectedTask(null); }}>
          <div className="bg-white rounded-lg w-full max-w-2xl shadow-lg p-6" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-lg font-semibold text-gray-800">{selectedTask?.title ?? 'Task details'}</h3>
                <div className="text-xs text-gray-500">ID: {selectedTaskId}</div>
              </div>
              <div className="flex items-center gap-2">
                <button onClick={() => { setSelectedTaskId(null); setSelectedTask(null); }} className="text-gray-500">✕</button>
              </div>
            </div>

            {detailsLoading && <div className="mt-4 text-sm text-gray-600">Loading...</div>}
            {detailsError && <div className="mt-4 text-sm text-red-600">{detailsError}</div>}

            {!detailsLoading && selectedTask && (
              <div className="mt-4 space-y-3 text-sm text-gray-800">
                <div>
                  <div className="text-xs text-gray-500">Title</div>
                  <div className="font-medium">{selectedTask.title ?? selectedTask.name}</div>
                </div>

                <div>
                  <div className="text-xs text-gray-500">Description</div>
                  <div>{selectedTask.description ?? '—'}</div>
                </div>

                <div>
                  <div className="text-xs text-gray-500">Date</div>
                  <div>{parseLocalDateTime(selectedTask.date)?.toLocaleString() ?? '-'}</div>
                </div>

                <div>
                  <div className="text-xs text-gray-500">Assigned to</div>
                  <div>{(selectedTask.assignedTo || []).map(a => a.fullName ?? a.username).join(', ') || '-'}</div>
                </div>

                <div>
                  <div className="text-xs text-gray-500">Completed</div>
                  <div>{selectedTask.completed ? 'Yes' : 'No'}</div>
                </div>

                <div className="flex gap-2 mt-4">
                  <button onClick={() => toggleComplete(selectedTaskId, !selectedTask.completed)}
                    className="px-3 py-2 bg-green-600 text-white rounded hover:bg-green-700 text-sm">
                    {selectedTask.completed ? 'Mark as not completed' : 'Mark completed'}
                  </button>

                  <button onClick={() => {
                    const newTitle = prompt('New title', selectedTask.title ?? selectedTask.name);
                    if (newTitle) changeTitle(selectedTaskId, newTitle);
                  }} className="px-3 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm">Edit title</button>

                  <button onClick={() => {
                    const nd = prompt('New date (YYYY-MM-DDTHH:mm:ss)', selectedTask.date);
                    if (nd) changeDate(selectedTaskId, nd);
                  }} className="px-3 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600 text-sm">Change date</button>

                  <button onClick={() => {
                    const ids = prompt('Assign to user ids (comma separated)', (selectedTask.assignedTo || []).map(a => a.id).join(','));
                    if (ids != null) reassign(selectedTaskId, ids.split(',').map(s => s.trim()).filter(Boolean));
                  }} className="px-3 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 text-sm">Reassign</button>

                  <button onClick={() => deleteTask(selectedTaskId)} className="px-3 py-2 bg-red-600 text-white rounded hover:bg-red-700 text-sm">Delete</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}