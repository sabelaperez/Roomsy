import React, { useState } from 'react';
import { groupApi } from '../api';

export default function GroupInfo({ groupInfo, loading, onUpdate }) {
  const [regenerateSuccess, setRegenerateSuccess] = useState('');
  const [regenerateError, setRegenerateError] = useState('');

  const regenerate = async () => {
    setRegenerateSuccess('');
    setRegenerateError('');
    if (!groupInfo?.id) {
      setRegenerateError('No group to regenerate invite for');
      return;
    }
    try {
      const data = await groupApi.regenerateInvite(groupInfo.id);
      onUpdate({ ...groupInfo, inviteCode: data.inviteCode });
      setRegenerateSuccess(`New invite code generated: ${data.inviteCode}`);
    } catch (e) {
      setRegenerateError(e.message || 'Failed to regenerate invite code');
    }
  };

  const copyInviteCode = async () => {
    if (groupInfo?.inviteCode) {
      try {
        await navigator.clipboard.writeText(groupInfo.inviteCode);
        setRegenerateSuccess('Invite code copied to clipboard!');
        setTimeout(() => setRegenerateSuccess(''), 2000);
      } catch (e) {
        setRegenerateError('Copy failed');
      }
    }
  };

  if (loading) return <div className="text-sm text-green-700">Loading group info...</div>;
  if (!groupInfo) return <div className="text-sm text-green-700">No group info</div>;

  return (
    <div className="space-y-4">
      <div className="bg-green-50 border border-green-200 p-4 rounded-md">
        <p className="text-green-800 font-medium mb-2">Your Group</p>

        <div>
          <p className="text-sm text-green-700">
            Group: <span className="font-semibold">{groupInfo.name}</span>
          </p>
          <p className="text-sm text-green-700">Members: {groupInfo.memberCount ?? '-'}</p>

          <div className="mt-3 flex items-center space-x-2">
            <div className="bg-white border border-green-300 px-3 py-2 rounded text-sm font-mono flex-1">
              {groupInfo.inviteCode}
            </div>
            <button
              onClick={copyInviteCode}
              className="bg-green-600 text-white px-3 py-2 rounded text-sm hover:bg-green-700 transition-colors"
            >
              Copy
            </button>
          </div>

            {regenerateSuccess && (
              <div className="mt-3 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded mb-3">
                {regenerateSuccess}
              </div>
            )}

            {regenerateError && (
              <div className="mt-3 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-3">
                {regenerateError}
              </div>
            )}
            
        </div>
      </div>

      {groupInfo && (
        <div className="border-t pt-4">
          <h3 className="text-lg font-semibold mb-3 text-gray-800">Manage Invite Code</h3>

          {regenerateSuccess && (
            <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded mb-3">
              {regenerateSuccess}
            </div>
          )}

          {regenerateError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-3">
              {regenerateError}
            </div>
          )}

          <button
            onClick={regenerate}
            className="w-full bg-orange-600 text-white py-2 px-4 rounded-md hover:bg-orange-700 transition-colors font-medium"
          >
            Regenerate Invite Code
          </button>

          <p className="text-xs text-gray-500 mt-2 text-center">
            This will invalidate the current invite code
          </p>
        </div>
      )}
    </div>
  );
}