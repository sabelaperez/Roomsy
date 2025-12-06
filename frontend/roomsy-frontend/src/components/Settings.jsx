import React, { useEffect, useState } from 'react';
import { authApi, groupApi, userApi } from '../api';

export default function Settings() {
  const [me, setMe] = useState(null);
  const [loadingMe, setLoadingMe] = useState(true);
  const [meError, setMeError] = useState('');

  const [group, setGroup] = useState(null);
  const [loadingGroup, setLoadingGroup] = useState(false);
  const [groupError, setGroupError] = useState('');

  const [members, setMembers] = useState([]);
  const [loadingMembers, setLoadingMembers] = useState(false);
  const [membersError, setMembersError] = useState('');

  useEffect(() => {
    let mounted = true;
    setLoadingMe(true);
    authApi.me()
      .then(data => { if (mounted) setMe(data); })
      .catch(e => { if (mounted) setMeError(e.message || 'Failed to load user'); })
      .finally(() => { if (mounted) setLoadingMe(false); });
    return () => { mounted = false; };
  }, []);

  useEffect(() => {
    if (!me) return;
    let mounted = true;
    setLoadingGroup(true);
    groupApi.get(me.userId)
      .then(g => {
        if (!mounted) return;
        setGroup(g);
        // load members if group present
        if (g?.id) {
          setLoadingMembers(true);
          groupApi.getMembers(g.id)
            .then(list => { if (mounted) setMembers(Array.isArray(list) ? list : []); })
            .catch(e => { if (mounted) setMembersError(e.message || 'Failed to load members'); })
            .finally(() => { if (mounted) setLoadingMembers(false); });
        } else {
          setMembers([]);
        }
      })
      .catch(e => { if (mounted) setGroupError(e.message || 'Failed to load group'); })
      .finally(() => { if (mounted) setLoadingGroup(false); });
    return () => { mounted = false; };
  }, [me]);

  const handleDeleteMe = async () => {
    if (!confirm('Delete your account? This action cannot be undone.')) return;
    try {
      setLoadingMe(true);
      await userApi.deleteMe();
      window.location.href = '/';
    } catch (e) {
      alert(e.message || 'Failed to delete account');
    } finally {
      setLoadingMe(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-8 max-w-5xl mx-auto mt-4">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">Settings</h2>

      <section className="bg-white shadow-md rounded-md p-6 mb-6">
        {loadingMe && <div className="text-sm text-gray-600">Loading user...</div>}
        {meError && <div className="text-sm text-red-600">{meError}</div>}

        {!loadingMe && me && (
          <div className="grid rounded gap-2">
            <div className="flex justify-between items-center">
              <div className="flex items-baseline gap-4 overflow-x-auto">
                <div className="text-lg font-semibold text-gray-800">{me.fullName ?? '—'}</div>
                <div className="text-sm text-gray-500">{"@"}{me.username ?? '—'}</div>
              </div>
              <button
                onClick={handleDeleteMe}
                className="px-3 py-1 bg-red-600 text-white rounded text-sm hover:bg-red-700"
               title="Delete account"
              >
                Delete account
              </button>
            </div>
            <div className="text-sm text-gray-800">{me.email ?? '—'}</div>
          </div>
        )}
      </section>

      <section>
        <h3 className="text-lg font-semibold mb-3 text-gray-800">Group Settings</h3>

        {loadingGroup && <div className="text-sm text-gray-600">Loading group...</div>}
        {groupError && <div className="text-sm text-red-600">{groupError}</div>}

        {!loadingGroup && !group && <div className="text-sm text-gray-600">You are not in a group</div>}

        {group && (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
              <div className="p-4 border rounded bg-gray-50">
                <div className="text-sm text-gray-500">Group name</div>
                <div className="text-base font-medium text-gray-800">{group.name ?? '—'}</div>
              </div>
              <div className="p-4 border rounded bg-gray-50">
                <div className="text-sm text-gray-500">Invite code</div>
                <div className="text-base font-medium text-gray-800">{group.inviteCode ?? '—'}</div>
              </div>
            </div>

            <div className="p-4 border rounded bg-gray-50">
              <div className="flex justify-between items-center mb-3">
                <div>
                  <div className="text-sm text-gray-500">Members</div>
                  <div className="text-base font-medium text-gray-800">{members.length} member(s)</div>
                </div>
                <button
                  className="px-3 py-1 bg-gray-100 rounded text-sm"
                  onClick={() => {
                    // refresh members
                    if (!group?.id) return;
                    setLoadingMembers(true);
                    groupApi.getMembers(group.id)
                      .then(list => setMembers(Array.isArray(list) ? list : []))
                      .catch(e => setMembersError(e.message || 'Failed to load members'))
                      .finally(() => setLoadingMembers(false));
                  }}
                >
                  Refresh
                </button>
              </div>

              {loadingMembers && <div className="text-sm text-gray-600">Loading members...</div>}
              {membersError && <div className="text-sm text-red-600">{membersError}</div>}

              {!loadingMembers && members.length > 0 && (
                <ul className="space-y-2">
                  {members.map(m => (
                    <li key={m.id} className="p-3 bg-white rounded border flex justify-between items-center">
                      <div>
                        <div className="text-sm font-medium text-gray-800">{m.fullName ?? m.username}</div>
                        <div className="text-xs text-gray-500">{m.email}</div>
                      </div>
                      <div className="text-xs text-gray-500">{m.active ? 'Active' : 'Inactive'}</div>
                    </li>
                  ))}
                </ul>
              )}

              {!loadingMembers && members.length === 0 && <div className="text-sm text-gray-600">No members found</div>}
            </div>
          </>
        )}
      </section>
    </div>
  );
}
// ...existing code...