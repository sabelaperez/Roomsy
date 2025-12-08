import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../../context/AuthContext';
import { expenseApi, groupApi } from '../../api';

export default function UserExpenses() {
  const { user } = useContext(AuthContext);
  const [group, setGroup] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [shared, setShared] = useState([]);

  useEffect(() => {
    if (!user) return;
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const g = await groupApi.get(user.userId);
        if (!mounted) return;
        setGroup(g);
      } catch (e) {
        if (!mounted) return;
        setError('No group');
        setGroup(null);
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => { mounted = false; };
  }, [user]);

  useEffect(() => {
    if (!group?.id) return;
    let mounted = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const page = await expenseApi.getSharedExpenses(group.id, { page: 0, size: 200 });
        if (!mounted) return;
        const list = page?.content ?? page?.items ?? [];
        setShared(Array.isArray(list) ? list : []);
      } catch (e) {
        if (!mounted) return;
        setError('Failed to load balances');
        setShared([]);
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => { mounted = false; };
  }, [group?.id]);

  const owesToUser = shared.filter(s => String(s.payer?.id) === String(user?.userId));
  const userOwes = shared.filter(s => String(s.notPaid?.id) === String(user?.userId));

  const totalOwesToUser = owesToUser.reduce((acc, s) => acc + (Number(s.quantity) || 0), 0);
  const totalUserOwes = userOwes.reduce((acc, s) => acc + (Number(s.quantity) || 0), 0);

  return (
    <div className="bg-white rounded-lg shadow-md p-4 w-full">
      <h3 className="text-lg font-semibold text-gray-800 mb-3">My Balances</h3>

      {loading && <div className="text-sm text-gray-600">Loading...</div>}
      {error && <div className="text-sm text-red-600">{error}</div>}

      {!loading && !error && (
        <div className="flex gap-4 flex-col">
          {/* Are own */}
          <div className="flex-1 min-w-[240px] bg-gray-50 border rounded p-4">
            <div className="flex items-baseline justify-between mb-3">
              <div>
                <div className="text-sm text-gray-500">You are owed</div>
                <div className="text-xl font-semibold text-gray-800">{totalOwesToUser.toFixed(2)} €</div>
              </div>
            </div>

            {owesToUser.length === 0 ? (
              <div className="text-sm text-gray-600">Nobody owes you</div>
            ) : (
              <ul className="space-y-2">
                {owesToUser.map(s => (
                  <li key={s.id} className="p-2 bg-white rounded border flex justify-between items-center">
                    <div className="text-sm text-gray-800">{s.notPaid?.fullName ?? s.notPaid?.username ?? '—'}</div>
                    <div className="text-sm font-medium text-gray-700">{(Number(s.quantity) || 0).toFixed(2)} €</div>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* You owe */}
          <div className="flex-1 min-w-[240px] bg-gray-50 border rounded p-4">
            <div className="flex items-baseline justify-between mb-3">
              <div>
                <div className="text-sm text-gray-500">You owe</div>
                <div className="text-xl font-semibold text-gray-800">{totalUserOwes.toFixed(2)} €</div>
              </div>
            </div>

            {userOwes.length === 0 ? (
              <div className="text-sm text-gray-600">You owe no one</div>
            ) : (
              <ul className="space-y-2">
                {userOwes.map(s => (
                  <li key={s.id} className="p-2 bg-white rounded border flex justify-between items-center">
                    <div className="text-sm text-gray-800">{s.payer?.fullName ?? s.payer?.username ?? '—'}</div>
                    <div className="text-sm font-medium text-gray-700">{(Number(s.quantity) || 0).toFixed(2)} €</div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}
    </div>
  );
}