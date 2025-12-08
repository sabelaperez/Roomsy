import React, { useState } from 'react';
import { groupApi, userApi } from '../../api';

export default function GroupCreateJoin({ user, onCreate, onJoin }) {
  const [groupName, setGroupName] = useState('');
  const [groupError, setGroupError] = useState('');
  const [groupSuccess, setGroupSuccess] = useState('');
  const [creating, setCreating] = useState(false);

  const [inviteCode, setInviteCode] = useState('');
  const [joinError, setJoinError] = useState('');
  const [joinSuccess, setJoinSuccess] = useState('');
  const [joining, setJoining] = useState(false);

  const handleCreate = async () => {
    setGroupError('');
    setGroupSuccess('');
    if (!user?.userId) {
      setGroupError('User not authenticated');
      return;
    }
    if (!groupName.trim()) {
      setGroupError('Please enter a group name');
      return;
    }
    setCreating(true);
    try {
      const data = await groupApi.create({ name: groupName.trim(), creatorId: user.userId });
      setGroupSuccess(`Group "${data.name}" created successfully! Invite code: ${data.inviteCode}`);
      setGroupName('');
      onCreate(data);
    } catch (e) {
      setGroupError(e.message || 'Failed to create group');
    } finally {
      setCreating(false);
    }
  };

  const handleJoin = async () => {
    setJoinError('');
    setJoinSuccess('');
    if (!user?.userId) {
      setJoinError('User not authenticated');
      return;
    }
    if (!inviteCode.trim()) {
      setJoinError('Please enter an invite code');
      return;
    }
    setJoining(true);
    try {
      const data = await userApi.joinByInvite(user.userId, inviteCode.trim());
      setJoinSuccess(`Successfully joined group "${data.name}"!`);
      setInviteCode('');
      onJoin(data);
    } catch (e) {
      setJoinError(e.message || 'Failed to join group');
    } finally {
      setJoining(false);
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div>
        <h3 className="text-lg font-semibold mb-4 text-gray-800">Create a Group</h3>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Group Name</label>
            <input
              type="text"
              value={groupName}
              onChange={(e) => setGroupName(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="My Awesome Group"
            />
            <p className="text-xs text-gray-500 mt-1">Letters, numbers, and spaces only (3-50 characters)</p>
          </div>

          {groupError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
              {groupError}
            </div>
          )}

          {groupSuccess && (
            <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
              {groupSuccess}
            </div>
          )}

          <button
            onClick={handleCreate}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors font-medium"
            disabled={creating}
          >
            {creating ? 'Creating...' : 'Create Group'}
          </button>
        </div>
      </div>

      <div className="border-t pt-6 md:border-t-0 md:border-l md:pl-6 md:pt-0">
        <h3 className="text-lg font-semibold mb-4 text-gray-800">Join a Group</h3>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Invite Code</label>
            <input
              type="text"
              value={inviteCode}
              onChange={(e) => setInviteCode(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleJoin()}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
              placeholder="Enter invite code"
            />
            <p className="text-xs text-gray-500 mt-1">Ask your roommate for the group invite code</p>
          </div>

          {joinError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
              {joinError}
            </div>
          )}

          {joinSuccess && (
            <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
              {joinSuccess}
            </div>
          )}

          <button
            onClick={handleJoin}
            className="w-full bg-green-600 text-white py-2 px-4 rounded-md hover:bg-green-700 transition-colors font-medium"
            disabled={joining}
          >
            {joining ? 'Joining...' : 'Join Group'}
          </button>
        </div>
      </div>
    </div>
  );
}