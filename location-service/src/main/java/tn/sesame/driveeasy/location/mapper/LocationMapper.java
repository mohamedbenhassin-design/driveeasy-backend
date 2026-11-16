package tn.sesame.driveeasy.location.mapper;

import tn.sesame.driveeasy.location.dto.LocationDtos.LocationResponse;
import tn.sesame.driveeasy.location.entity.Location;

/** Conversion entité Location -> DTO. */
public final class LocationMapper {

    private LocationMapper() {}

    public static LocationResponse toResponse(Location l) {
        return new LocationResponse(l.getId(), l.getClientId(), l.getVehiculeId(), l.getDateDebut(),
                l.getDateFinPrevue(), l.getDateRetourEffectif(), l.getKmDepart(), l.getKmRetour(),
                l.getPrixJournalierApplique(), l.getMontantRemise(), l.getMontantTotal(), l.getPenalite(),
                l.getStatut(), l.getDateCreation());
    }
}
