package be.vinci.pae.domain.interfaces;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import be.vinci.pae.domain.VisitImpl;

@JsonDeserialize(as = VisitImpl.class)
public interface VisitDTO {

  enum VisitCondition {
    EN_ATTENTE("en attente"), ACCEPTEE("acceptée"), ANNULEE("annulée");

    private String visitCondition;

    VisitCondition(String visitCondition) {
      this.visitCondition = visitCondition;
    }

    public String toString() {
      return this.visitCondition;
    }
  }

  int getIdRequest();

  void setIdRequest(int idRequest);

  String getTimeSlot();

  void setTimeSlot(String timeSlot);

  int getWarehouseAddressId();

  void setWarehouseAddressId(int id);

  AddressDTO getWarehouseAddress();

  void setWarehouseAddress(AddressDTO address);

  VisitCondition getVisitCondition();

  void setVisitCondition(String visitCondition);

  UserDTO getClient();

  void setClient(UserDTO userDTO);

  int getIdClient();

  void setIdClient(int idClient);

  String getExplanatoryNote();

  void setExplanatoryNote(String explanatoryNote);

  LocalDateTime getScheduledDateTime();

  void setScheduledDateTime(LocalDateTime scheduledDateTime);

  List<FurnitureDTO> getFurnitureList();

  void setFurnitureList(List<FurnitureDTO> furnitureList);

  int getAmountOfFurnitures();

  void setAmountOfFurnitures(int amountOfFurnitures);

  LocalDateTime getRequestDateTime();

  void setRequestDateTime(LocalDateTime requestDateTime);

}
