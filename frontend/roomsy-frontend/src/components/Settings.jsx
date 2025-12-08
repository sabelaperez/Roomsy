import React, { useEffect, useState } from 'react';
import { authApi, groupApi, userApi } from '../api';
import ConfirmModal from './ConfirmModal';

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

  const [regenerateSuccess, setRegenerateSuccess] = useState('');
  const [regenerateError, setRegenerateError] = useState('');

  const [editingName, setEditingName] = useState(false);
  const [newGroupName, setNewGroupName] = useState('');
  const [opLoading, setOpLoading] = useState(false);
  const [opError, setOpError] = useState('');
  const [opSuccess, setOpSuccess] = useState('');
  const [removeMemberLoadingId, setRemoveMemberLoadingId] = useState(null);

  const [confirmModal, setConfirmModal] = useState({
    isOpen: false,
    title: '',
    message: '',
    onConfirm: null,
    confirmText: 'Confirm',
    variant: 'danger'
  });

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

  const regenerate = async () => {
    setRegenerateSuccess('');
    setRegenerateError('');
    if (!group?.id) {
      setRegenerateError('No group to regenerate invite for');
      return;
    }
    try {
      const data = await groupApi.regenerateInvite(group.id);
      setGroup({ ...group, inviteCode: data.inviteCode });
      setRegenerateSuccess(`New invite code generated: ${data.inviteCode}`);
    } catch (e) {
      setRegenerateError(e.message || 'Failed to regenerate invite code');
    }
  };

  const copyInviteCode = async () => {
    if (group?.inviteCode) {
      try {
        await navigator.clipboard.writeText(group.inviteCode);
        setRegenerateSuccess('Invite code copied to clipboard!');
        setTimeout(() => setRegenerateSuccess(''), 2000);
      } catch (e) {
        setRegenerateError('Copy failed');
      }
    }
  };

  const handleDeleteMe = () => {
    setConfirmModal({
      isOpen: true,
      title: 'Delete Account',
      message: 'Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently removed.',
      confirmText: 'Delete Account',
      variant: 'danger',
      onConfirm: async () => {
        try {
          setLoadingMe(true);
          await userApi.deleteMe();
          window.location.href = '/';
        } catch (e) {
          setOpError(e.message || 'Failed to delete account');
          setConfirmModal({ ...confirmModal, isOpen: false });
        } finally {
          setLoadingMe(false);
        }
      }
    });
  };

  const leaveGroup = () => {
    setConfirmModal({
      isOpen: true,
      title: 'Leave Group',
      message: 'Are you sure you want to leave this group? You will lose access to all shared expenses and data.',
      confirmText: 'Leave Group',
      variant: 'danger',
      onConfirm: async () => {
        if (!group?.id) return;
        setOpLoading(true); setOpError(''); setOpSuccess('');
        try {
          await groupApi.removeMember(group.id, me.userId);
          setOpSuccess('You left the group');
          setTimeout(() => {
            window.location.href = '/';
          }, 1000);
        } catch (e) {
          setOpError(e.message || 'Failed to leave group');
        } finally { 
          setOpLoading(false); 
          setConfirmModal({ ...confirmModal, isOpen: false });
        }
      }
    });
  };

  const removeMember = (memberId, memberName) => {
    setConfirmModal({
      isOpen: true,
      title: 'Remove Member',
      message: `Are you sure you want to remove ${memberName} from the group?`,
      confirmText: 'Remove Member',
      variant: 'danger',
      onConfirm: async () => {
        if (!group?.id) return;
        setRemoveMemberLoadingId(memberId); setOpError(''); setOpSuccess('');
        try {
          await groupApi.removeMember(group.id, memberId);
          setMembers(m => m.filter(mb => mb.id !== memberId));
          setOpSuccess('Member removed');
        } catch (e) {
          setOpError(e.message || 'Failed to remove member');
        } finally { 
          setRemoveMemberLoadingId(null);
          setConfirmModal({ ...confirmModal, isOpen: false });
        }
      }
    });
  };

  const deleteGroup = () => {
    setConfirmModal({
      isOpen: true,
      title: 'Delete Group',
      message: 'Are you sure you want to delete this group? This action is irreversible and will delete all expenses, balances, and member data.',
      confirmText: 'Delete Group',
      variant: 'danger',
      onConfirm: async () => {
        if (!group?.id) return;
        setOpLoading(true); setOpError(''); setOpSuccess('');
        try {
          await groupApi.delete(group.id);
          setOpSuccess('Group deleted');
          setTimeout(() => {
            window.location.href = '/';
          }, 1000);
        } catch (e) {
          setOpError(e.message || 'Failed to delete group');
        } finally { 
          setOpLoading(false);
          setConfirmModal({ ...confirmModal, isOpen: false });
        }
      }
    });
  };

  const saveName = async () => {
    if (!group?.id) return;
    if (!newGroupName || newGroupName.trim().length === 0) {
      setOpError('Name cannot be empty');
      return;
    }
    setOpLoading(true); setOpError(''); setOpSuccess('');
    try {
      const updated = await groupApi.updateName(group.id, newGroupName.trim());
      setGroup(g => ({ ...g, name: updated.name ?? newGroupName.trim() }));
      setEditingName(false);
      setOpSuccess('Group name updated');
    } catch (e) {
      setOpError(e.message || 'Failed to update name');
    } finally { setOpLoading(false); }
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

      <h2 className="text-2xl font-bold mb-4 text-gray-800">Settings</h2>

      {opSuccess && (
        <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded mb-4">
          {opSuccess}
        </div>
      )}
      {opError && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {opError}
        </div>
      )}

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
        {groupError && loadingGroup && <div className="text-sm text-red-600">{groupError}</div>}

        {!loadingGroup && !group && <div className="text-sm text-gray-600">You are not in a group</div>}

        {group && (
          <div className="bg-white shadow-md rounded-md grid grid-cols-1 md:grid-cols-3 gap-10 mb-4 p-6">
            <div className="rounded space-y-6">
              <div className='flex item-start justify-between mb-4'>
                <div>
                  <div className="text-sm text-gray-500">Group name</div>
                  <div className="text-base font-medium text-gray-800">{group.name ?? '—'}</div>
                </div>
                <div className="flex items-center gap-2">
                  {!editingName && (
                    <button
                      onClick={() => { setEditingName(true); setNewGroupName(group.name ?? ''); }}
                      className="px-2 py-1 bg-gray-100 rounded text-sm"
                      title="Edit group name"
                    >
                      Edit
                    </button>
                  )}
                  {editingName && (
                    <div className='flex flex-col justify-end gap-2'>
                      <input
                        value={newGroupName}
                        onChange={e => setNewGroupName(e.target.value)}
                        className="text-sm max-w-[150px] border px-2 py-1 rounded"
                      />
                      <div className='flex items-center justify-center gap-2'>
                        <button onClick={saveName} disabled={opLoading} className="px-2 py-1 bg-green-600 text-white rounded text-sm">Save</button>
                        <button onClick={() => setEditingName(false)} className="px-2 py-1 bg-gray-100 rounded text-sm">Cancel</button>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              <div className='flex item-start justify-between mb-4'>
                <div>
                  <div className="text-xs text-gray-500 py-1">Invite Code</div>
                  <div className="text-sm font-medium text-gray-800">{group.inviteCode ?? '—'}</div>
                </div>
                <div className="flex flex-col items-end gap-2">
                  <div className="flex items-center gap-2">
                    {!editingName && (
                      <button
                        onClick={copyInviteCode}
                        className="px-2 py-1 bg-gray-100 rounded text-sm"
                        title="Copy invite code"
                      >
                        Copy
                      </button>
                    )}
                    {!editingName && (
                      <button
                        onClick={regenerate}
                        className="px-2 py-1 bg-gray-100 rounded text-sm"
                        title="Regenerate invite code"
                      >
                        Regenerate
                      </button>
                    )}
                  </div>
                </div>
              </div>

              {(regenerateSuccess || regenerateError) && (
                <div className={`text-xs p-2 rounded ${regenerateSuccess ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>
                  {regenerateSuccess || regenerateError}
                </div>
              )}

              <div>
                <div className='flex flex-col gap-2'>
                  <div className="text-xs text-gray-500">Warning Zone</div>
                  <button
                    onClick={leaveGroup}
                    disabled={opLoading}
                    className="px-2 py-1 bg-red-600 text-white rounded text-sm hover:bg-red-700"
                    title="Leave group"
                  >
                    Leave group
                  </button>
                  <button
                    onClick={deleteGroup}
                    disabled={opLoading}
                    className="px-2 py-1 bg-red-600 text-white rounded text-sm hover:bg-red-700"
                    title="Delete group"
                  >
                    Delete group
                  </button>
                </div>
              </div>
            </div>

            <div className="rounded col-span-2">
              <div className="text-sm font-medium text-gray-800">Members</div>
              <div className="text-sm text-gray-600">{members.length} member(s)</div>

              {loadingMembers && <div className="text-sm text-gray-600 mt-2">Loading members...</div>}
              {membersError && <div className="text-sm text-red-600 mt-2">{membersError}</div>}

              {!loadingMembers && members.length > 0 && (
                <ul className="space-y-2 mt-2">
                  {members.map(m => (
                    <li key={m.id} className="p-3 border rounded bg-gray-50 flex justify-between items-center">
                      <div>
                        <div className="text-sm font-medium text-gray-800">{m.fullName ?? m.username}</div>
                        <div className="text-xs text-gray-500">{m.email}</div>
                      </div>
                      {m.id !== me.userId ? (
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => removeMember(m.id, m.fullName ?? m.username)}
                            disabled={removeMemberLoadingId === m.id}
                            className="px-2 py-1 bg-red-100 text-red-700 rounded text-xs"
                            title="Remove member"
                          >
                            {removeMemberLoadingId === m.id ? 'Removing...' : 'Remove'}
                          </button>
                        </div>
                      ) : null}
                    </li>
                  ))}
                </ul>
              )}

              {!loadingMembers && members.length === 0 && <div className="text-sm text-gray-600">No members found</div>}
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
