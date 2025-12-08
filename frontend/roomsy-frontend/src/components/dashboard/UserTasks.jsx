import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../context/AuthContext';
import { groupApi, tasksApi } from '../../api';

export default function UserTasks({ maxItems = 6 }) {
  const { user } = useContext(AuthContext);
  const [group, setGroup] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    if (!user) return;
    let mounted = true;
    setLoading(true);
    groupApi.get(user.userId)
      .then(g => { if (mounted) setGroup(g); })
      .catch(() => { if (mounted) setGroup(null); })
      .finally(() => { if (mounted) setLoading(false); });
    return () => { mounted = false; };
  }, [user]);

  useEffect(() => {
    if (!group?.id) return;
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const page = await tasksApi.getGroupTasks(group.id, { page: 0, size: 10, sortBy: 'date', sortDirection: 'asc' });
        const list = page?.content ?? page?.items ?? [];
        if (!mounted) return;
        const now = Date.now();
        const myPending = (Array.isArray(list) ? list : [])
          .filter(t => !t.completed)
          .filter(t => {
            const assigned = Array.isArray(t.assignedTo) ? t.assignedTo : [];
            return assigned.some(a => String(a?.id ?? a?.userId ?? a) === String(user?.userId));
          })
          .filter(t => {
            const dt = t.date ?? t.taskDate ?? t.dueDate ?? t.createdAt;
            if (!dt) return true;
            const d = new Date(dt);
            return !isNaN(d) ? d.getTime() >= now : true;
          })
          .sort((a, b) => {
            const da = new Date(a.date ?? a.taskDate ?? a.dueDate ?? a.createdAt).getTime() || 0;
            const db = new Date(b.date ?? b.taskDate ?? b.dueDate ?? b.createdAt).getTime() || 0;
            return da - db;
          })
          .slice(0, maxItems);
        setTasks(myPending);
      } catch (e) {
        if (!mounted) return;
        setError(e.message || 'Failed to load tasks');
        setTasks([]);
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => { mounted = false; };
  }, [group?.id, user?.userId, maxItems]);

  const formatDate = (iso) => {
    if (!iso) return '-';
    const d = new Date(iso);
    if (isNaN(d)) return '-';
    return d.toLocaleString();
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-4 w-full">
      <h3 className="text-lg font-semibold text-gray-800 mb-3">Upcoming Tasks</h3>

      {loading && <div className="text-sm text-gray-600">Loading tasks...</div>}
      {error && <div className="text-sm text-red-600">{error}</div>}

      {!loading && !error && (
        <div className="flex gap-4 flex-col py-2">
          {tasks.length === 0 ? (
            <div className="text-sm text-gray-600">No upcoming pending tasks</div>
          ) : (
            tasks.map(t => (
              <div key={t.id} className="min-w-[220px] bg-gray-50 border rounded p-3 flex-shrink-0">
                <div className="text-sm text-gray-500 mb-1">{formatDate(t.date ?? t.taskDate ?? t.dueDate ?? t.createdAt)}</div>
                <div className="text-md font-semibold text-gray-800 mb-1">{t.title ?? t.name ?? 'Task'}</div>
                <div className="text-xs text-gray-600 mb-2">{(t.assignedTo || []).map(a => a?.fullName ?? a?.username ?? (a?.id ?? a)).join(', ')}</div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}