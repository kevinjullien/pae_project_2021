package be.vinci.pae.domain.visit;

import java.time.LocalDateTime;
import java.util.List;
import be.vinci.pae.domain.furniture.FurnitureDTO;

public interface VisitUCC {

  List<VisitDTO> getNotConfirmedVisits();

  boolean submitRequestOfVisit(VisitDTO visit);

  boolean acceptVisit(int idVisit, LocalDateTime scheduledDateTime);

  boolean cancelVisit(int idVisit, String explanatoryNote);

  VisitDTO getVisitById(int id);

  List<FurnitureDTO> getListFurnituresForOneVisit(int idVisit);

}
