package org.acme.Services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.PGDB.DTOs.PublisherDTO;
import org.acme.PGDB.Entities.Publisher;
import org.acme.PGDB.Mappers.PublisherMapper;
import org.acme.PGDB.Repositories.PublisherRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PublisherService {

    @Inject
    PublisherMapper publisherMapper;

    @Inject
    PublisherRepository publisherRepository;

    @Transactional
    public PublisherDTO create(PublisherDTO dto) {
        Publisher entity = publisherMapper.toEntity(dto);
        publisherRepository.persist(entity);
        return publisherMapper.toDTO(entity);
    }

    public List<PublisherDTO> getAll() {
        return publisherRepository.listAll().stream()
                .map(publisherMapper::toDTO)
                .collect(Collectors.toList());
    }
}
