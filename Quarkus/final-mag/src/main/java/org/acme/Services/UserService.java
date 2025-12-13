package org.acme.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.PGDB.DTOs.DeveloperDTO;
import org.acme.PGDB.DTOs.UserDTO;
import org.acme.PGDB.Entities.User;
import org.acme.PGDB.Mappers.UserMapper;
import org.acme.PGDB.Repositories.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    @Inject
    ImageService imageService;

    @Transactional
    public void createUser(UUID id, String displayName) {
        User user = new User();
        user.setId(id);
        user.setDisplayName(displayName);
        user.setBio(""); // Default empty
        user.setAvatarUrl(imageService.getDefaultCoverUrl()); // Default null

        userRepository.persist(user);
    }

    @Transactional
    public UserDTO updateUser(UserDTO dto) {
        User user = userRepository.findById(dto.getId());

        user.setBio(dto.getBio());
        user.setAvatarUrl(dto.getAvatarUrl());
        userRepository.persist(user);

        return userMapper.toDTO(user);
    }

    public User getById(UUID id) {
        return userRepository.findById(id);
    }

    public List<UserDTO> getAll() {
        List<UserDTO> userList = userRepository.listAll().stream()
                .map(userMapper::toDTO)
                .toList();

        for (UserDTO userDTO : userList)
            userDTO.setUserGameListEntries(null);

        return userList;
    }
}
