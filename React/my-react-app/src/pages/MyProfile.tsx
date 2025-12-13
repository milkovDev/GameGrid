import React, { useEffect, useState, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import ProfileInfo from '../components/users/ProfileInfo';
import GameListManager from '../components/ugles/GameListManager';
import ProfileActions from '../components/users/ProfileActions';
import { UserGameListEntryDTO } from '../types/UserGameListEntryDTO';
import UserConnectionsModal from '../components/users/UserConnectionsModal';
import { useUser } from '../contexts/UserContext';
import '../styles/ProfileStyles.css';

const MyProfile: React.FC = () => {
  const { isLoading } = useAuth();
  const { userData, fetchUserData, updateUgle, deleteUgle } = useUser();
  const [isUpdating, setIsUpdating] = useState(false);
  const [showFollowingModal, setShowFollowingModal] = useState(false);
  const [showFollowersModal, setShowFollowersModal] = useState(false);

  useEffect(() => {
    if (!userData) {
      fetchUserData();
    }
  }, [userData, fetchUserData]);

  const handleUgleUpdate = useCallback((updatedUgle: UserGameListEntryDTO) => {
    setIsUpdating(true);
    setTimeout(() => {
      updateUgle(updatedUgle);
      setIsUpdating(false);
    }, 50);
  }, [updateUgle]);

  const handleUgleDelete = useCallback((ugleId: number) => {
    setIsUpdating(true);
    setTimeout(() => {
      deleteUgle(ugleId);
      setIsUpdating(false);
    }, 50);
  }, [deleteUgle]);

  if (isLoading || !userData) {
    return <div className="text-center mt-5">Loading...</div>;
  }

  const ugles = userData.relationalData.userGameListEntries || [];

  return (
    <div className="container mt-4 px-0">
      <div className="mx-3">
        <ProfileInfo user={userData.relationalData} isOwn={true} />
        
        <ProfileActions
          isOwnProfile={true}
          onShowFollowing={() => setShowFollowingModal(true)}
          onShowFollowers={() => setShowFollowersModal(true)}
        />
        
        <GameListManager
          ugles={ugles}
          userName=""
          isOwn={true}
          isUpdating={isUpdating}
          onUpdate={handleUgleUpdate}
          onDelete={handleUgleDelete}
        />
        
        <UserConnectionsModal
          show={showFollowingModal}
          onClose={() => setShowFollowingModal(false)}
          userId={userData.relationalData.id}
          type="following"
          isOwnProfile={true}
        />
        <UserConnectionsModal
          show={showFollowersModal}
          onClose={() => setShowFollowersModal(false)}
          userId={userData.relationalData.id}
          type="followers"
          isOwnProfile={true}
        />
      </div>
    </div>
  );
};

export default MyProfile;