package it.polito.ai.virtualLabs.repositories;

import it.polito.ai.virtualLabs.entities.Vm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VmRepository extends JpaRepository<Vm, Long> {

    Optional<Vm> findVmByTeamIdAndCreatorIdAndVmModelId(Long teamId, String studentId, Long vmModelId);
    List<Vm> findAllByVmModelId(Long vmModelId);
}
