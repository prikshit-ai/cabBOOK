package com.cabbooking.dao;

import com.cabbooking.exceptions.DuplicateVehicleException;
import com.cabbooking.model.Cab;
import java.util.List;

public interface CabDAO {
    boolean addCab(Cab cab) throws DuplicateVehicleException;
    void updateCab(int cabId, String driverName, String vehicleNumber, boolean isAvailable, String cabType);
    void deleteCab(String vehicleNumber);
    List<Cab> getAllCabs(); // Now actually returns the list
    List<Cab> getAvailableCabs(); // Now actually returns the list
    Cab getCabByVehicleNumber(String vehicleNumber); // Changed return type to Cab
    boolean updateCabAvailability(String vehicleNumber, boolean isAvailable);
}




