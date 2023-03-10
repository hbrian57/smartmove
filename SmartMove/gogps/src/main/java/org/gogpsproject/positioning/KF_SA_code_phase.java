package org.gogpsproject.positioning;

import java.util.ArrayList;

import org.ejml.simple.SimpleMatrix;
import org.gogpsproject.Constants;
import org.gogpsproject.GoGPS;
import org.gogpsproject.consumer.PositionConsumer;
import org.gogpsproject.positioning.RoverPosition.DopType;
import org.gogpsproject.producer.Observations;
import org.gogpsproject.producer.ObservationsProducer;

public class KF_SA_code_phase extends KalmanFilter {

  public KF_SA_code_phase(GoGPS goGPS) {
    super(goGPS);
  }

  @Override
  void setup(Observations roverObs, Observations masterObs, Coordinates masterPos) {    // Definition of matrices

    // Number of GPS observations
    int nObs = roverObs.getNumSat();

    // Number of available satellites (i.e. observations)
    int nObsAvail = sats.avail.size();

    // Matrix containing parameters obtained from the linearization of the observation equations
    SimpleMatrix A = new SimpleMatrix(nObsAvail, 3);

    // Counter for available satellites
    int k = 0;

    // Counter for satellites with phase available
    int p = 0;

    for (int i = 0; i < nObs; i++) {

      int id = roverObs.getSatID(i);
      char satType = roverObs.getGnssType(i);
      String checkAvailGnss = String.valueOf(satType) + String.valueOf(id);

      if (sats.pos[i]!=null && sats.gnssAvail.contains(checkAvailGnss)) {

        // Compute parameters obtained from linearization of observation equations
        double alphaX = rover.diffSat[i].get(0) / rover.satAppRange[i];
        double alphaY = rover.diffSat[i].get(1) / rover.satAppRange[i];
        double alphaZ = rover.diffSat[i].get(2) / rover.satAppRange[i];

        // Fill in the A matrix
        A.set(k, 0, alphaX); /* X */
        A.set(k, 1, alphaY); /* Y */
        A.set(k, 2, alphaZ); /* Z */

        // Observed code
        double obsRangeCode = roverObs.getSatByIDType(id, satType).getPseudorange(goGPS.getFreq());

        // Observed phase
        double obsRangePhase = roverObs.getSatByIDType(id, satType).getPhaserange(goGPS.getFreq());

        // Compute approximate ranges
        double appRangeCode;
        double appRangePhase;
        if( goGPS.getFreq() == 0) {
          appRangeCode = rover.satAppRange[i] + Constants.SPEED_OF_LIGHT*(rover.clockError - sats.pos[i].getSatelliteClockError()) + rover.satTropoCorr[i] + rover.satIonoCorr[i];
          appRangePhase = rover.satAppRange[i] + Constants.SPEED_OF_LIGHT*(rover.clockError - sats.pos[i].getSatelliteClockError()) + rover.satTropoCorr[i] - rover.satIonoCorr[i];
        } else {
          appRangeCode = rover.satAppRange[i] + Constants.SPEED_OF_LIGHT*(rover.clockError - sats.pos[i].getSatelliteClockError()) + rover.satTropoCorr[i] + rover.satIonoCorr[i] * Math.pow(roverObs.getSatByIDType(id, satType).getWavelength(1)/roverObs.getSatByIDType(id, satType).getWavelength(0), 2);
          appRangePhase = rover.satAppRange[i] + Constants.SPEED_OF_LIGHT*(rover.clockError - sats.pos[i].getSatelliteClockError()) + rover.satTropoCorr[i] - rover.satIonoCorr[i] * Math.pow(roverObs.getSatByIDType(id, satType).getWavelength(1)/roverObs.getSatByIDType(id, satType).getWavelength(0), 2);
        }

        // Fill in one row in the design matrix (for code)
        H.set(k, 0, alphaX);
        H.set(k, i1 + 1, alphaY);
        H.set(k, i2 + 1, alphaZ);

        // Fill in one element of the observation vector (for code)
        y0.set(k, 0, obsRangeCode - appRangeCode + alphaX * rover.getX() + alphaY * rover.getY() + alphaZ * rover.getZ());

        // Fill in the observation error covariance matrix (for code)
        double roverSatWeight = computeWeight(rover.topo[i].getElevation(), roverObs.getSatByIDType(id, satType).getSignalStrength(goGPS.getFreq()));
        double CnnBase = Cnn.get(k, k);
        Cnn.set(k, k, CnnBase + Math.pow(getStDevCode(roverObs.getSatByIDType(id, satType), goGPS.getFreq()), 2) * roverSatWeight);

        if (sats.gnssAvail.contains(checkAvailGnss)){
//        if (sats.availPhase.contains(id) && sats.typeAvailPhase.contains(satType)) {

          // Fill in one row in the design matrix (for phase)
          H.set(nObsAvail + p, 0, alphaX);
          H.set(nObsAvail + p, i1 + 1, alphaY);
          H.set(nObsAvail + p, i2 + 1, alphaZ);
          H.set(nObsAvail + p, i3 + id, -roverObs.getSatByIDType(id, satType).getWavelength(goGPS.getFreq()));

          // Fill in one element of the observation vector (for phase)
          y0.set(nObsAvail + p, 0, obsRangePhase - appRangePhase + alphaX * rover.getX() + alphaY * rover.getY() + alphaZ * rover.getZ());

          // Fill in the observation error covariance matrix (for phase)
          CnnBase = Cnn.get(nObsAvail + p, nObsAvail + p);
          Cnn.set(nObsAvail + p, nObsAvail + p, CnnBase 
              + Math.pow(stDevPhase, 2) * roverSatWeight);

          // Increment satellites with phase counter
          p++;
        }

        // Increment available satellites counter
        k++;
      }
    }

    updateDops(A);
  }

  /**
   * @param roverObs
   */
  @Override
  void estimateAmbiguities( Observations roverObs, Observations masterObs, Coordinates masterPos, ArrayList<Integer> satAmb, int pivotIndex, boolean init){
  
    // Number of GPS observations
    int nObs = roverObs.getNumSat();
  
    // Number of available satellites (i.e. observations)
    int nObsAvail = sats.avail.size();
  
    // Number of available satellites (i.e. observations) with phase
    int nObsAvailPhase = sats.availPhase.size();
  
    // Number of unknown parameters
    int nUnknowns = 4 + satAmb.size();
  
    // Estimated ambiguities
    double[] estimatedAmbiguities = new double[satAmb.size()];
  
    // Covariance of estimated ambiguity combinations
    double[] estimatedAmbiguitiesCovariance = new double[satAmb.size()];
  
    // Least squares design matrix
    SimpleMatrix A = new SimpleMatrix(nObsAvail+nObsAvailPhase, nUnknowns);
  
    // Vector for approximate pseudoranges
    SimpleMatrix b = new SimpleMatrix(nObsAvail+nObsAvailPhase, 1);
  
    // Vector for observed pseudoranges
    SimpleMatrix y0 = new SimpleMatrix(nObsAvail+nObsAvailPhase, 1);
  
    // Cofactor matrices
    SimpleMatrix Qcode = new SimpleMatrix(nObsAvail, nObsAvail);
    SimpleMatrix Qphase = new SimpleMatrix(nObsAvailPhase, nObsAvailPhase);
    SimpleMatrix Q = new SimpleMatrix(nObsAvail+nObsAvailPhase, nObsAvail+nObsAvailPhase);
  
    // Solution vector
    SimpleMatrix x = new SimpleMatrix(nUnknowns, 1);
  
    // Vector for observation error
    SimpleMatrix vEstim = new SimpleMatrix(nObsAvail, 1);
  
    // Error covariance matrix
    SimpleMatrix covariance = new SimpleMatrix(nUnknowns, nUnknowns);
  
    // Vectors for troposphere and ionosphere corrections
    SimpleMatrix tropoCorr = new SimpleMatrix(nObsAvail+nObsAvailPhase, 1);
    SimpleMatrix ionoCorr = new SimpleMatrix(nObsAvail+nObsAvailPhase, 1);
  
    // Counters for available satellites
    int k = 0;
    int p = 0;
  
    // Set up the least squares matrices...
    // ... for code ...
    for (int i = 0; i < nObs; i++) {
  
      int id = roverObs.getSatID(i);
      char satType = roverObs.getGnssType(i);
      String checkAvailGnss = String.valueOf(satType) + String.valueOf(id);
  
      if (sats.pos[i] !=null && sats.gnssAvail.contains(checkAvailGnss)) {
  
        // Fill in one row in the design matrix
        A.set(k, 0, rover.diffSat[i].get(0) / rover.satAppRange[i]); /* X */
        A.set(k, 1, rover.diffSat[i].get(1) / rover.satAppRange[i]); /* Y */
        A.set(k, 2, rover.diffSat[i].get(2) / rover.satAppRange[i]); /* Z */
  
        A.set(k, 3, 1); /* clock error */
  
        // Add the approximate pseudorange value to b
        b.set(k, 0, rover.satAppRange[i] - sats.pos[i].getSatelliteClockError() * Constants.SPEED_OF_LIGHT);
  
        // Add the observed pseudorange value to y0
        y0.set(k, 0, roverObs.getSatByIDType(id, satType).getPseudorange(goGPS.getFreq()));
  
        // Fill in troposphere and ionosphere double corrections
        tropoCorr.set(k, 0, rover.satTropoCorr[i]);
        ionoCorr.set(k, 0, rover.satIonoCorr[i]);
  
        // Fill in the cofactor matrix
        double roverSatWeight = computeWeight(rover.topo[i].getElevation(), roverObs.getSatByIDType(id, satType).getSignalStrength(goGPS.getFreq()));
        Qcode.set(k, k, Qcode.get(k, k) + getStDevCode(roverObs.getSatByID(id), goGPS.getFreq()) * roverSatWeight);
  
        // Increment available satellites counter
        k++;
      }
    }
  
    // ... and phase
    for (int i = 0; i < nObs; i++) {
  
      int id = roverObs.getSatID(i);
      char satType = roverObs.getGnssType(i);
      String checkAvailGnss = String.valueOf(satType) + String.valueOf(id);
  
      if (sats.pos[i] !=null && sats.gnssAvail.contains(checkAvailGnss)) {
  
        // Fill in one row in the design matrix
        A.set(k, 0, rover.diffSat[i].get(0) / rover.satAppRange[i]); /* X */
        A.set(k, 1, rover.diffSat[i].get(1) / rover.satAppRange[i]); /* Y */
        A.set(k, 2, rover.diffSat[i].get(2) / rover.satAppRange[i]); /* Z */
        A.set(k, 3, 1); /* clock error */
  
        if (satAmb.contains(id)) {
          A.set(k, 4 + satAmb.indexOf(id), -roverObs.getSatByIDType(id, satType).getWavelength(goGPS.getFreq())); /* N */
  
          // Add the observed phase range value to y0
          y0.set(k, 0, roverObs.getSatByIDType(id, satType).getPhaserange(goGPS.getFreq()));
        } 
        else {
          // Add the observed phase range value + known N to y0
          y0.set(k, 0, roverObs.getSatByIDType(id, satType).getPhaserange(goGPS.getFreq()) + KFprediction.get(i3 + id) * roverObs.getSatByIDType(id, satType).getWavelength(goGPS.getFreq()));
        }
  
        // Add the approximate pseudorange value to b
        b.set(k, 0, rover.satAppRange[i] - sats.pos[i].getSatelliteClockError() * Constants.SPEED_OF_LIGHT);
  
        // Fill in troposphere and ionosphere corrections
        tropoCorr.set(k, 0, rover.satTropoCorr[i]);
        ionoCorr.set(k, 0, -rover.satIonoCorr[i]);
  
        // Fill in the cofactor matrix
        double roverSatWeight = computeWeight(rover.topo[i].getElevation(), roverObs.getSatByIDType(id, satType).getSignalStrength(goGPS.getFreq()));
        Qphase.set(p, p, Qphase.get(p, p) + Math.pow(stDevPhase, 2) * roverSatWeight);
        
        int r = 1;
        for (int m = i+1; m < nObs; m++) {
          if (sats.pos[m] !=null && sats.availPhase.contains(sats.pos[m].getSatID())) {
            Qphase.set(p, p+r, 0);
            Qphase.set(p+r, p, 0);
            r++;
          }
        }
  
        // Increment available satellite counters
        k++;
        p++;
      }
    }
  
    // Apply troposphere and ionosphere correction
    b = b.plus(tropoCorr);
    b = b.plus(ionoCorr);
  
    //Build complete cofactor matrix (code and phase)
    Q.insertIntoThis(0, 0, Qcode);
    Q.insertIntoThis(nObsAvail, nObsAvail, Qphase);
  
    // Least squares solution x = ((A'*Q^-1*A)^-1)*A'*Q^-1*(y0-b);
    x = A.transpose().mult(Q.invert()).mult(A).invert().mult(A.transpose()).mult(Q.invert()).mult(y0.minus(b));
  
    // Receiver clock error
    rover.clockError = x.get(3) / Constants.SPEED_OF_LIGHT;
  
    // Estimation of the variance of the observation error
    vEstim = y0.minus(A.mult(x).plus(b));
    double varianceEstim = (vEstim.transpose().mult(Q.invert()).mult(vEstim)).get(0) / (nObsAvail + nObsAvailPhase - nUnknowns);
  
    // Covariance matrix of the estimation error
    covariance = A.transpose().mult(Q.invert()).mult(A).invert().scale(varianceEstim);
  
    // Store estimated ambiguity combinations and their covariance
    for (int m = 0; m < satAmb.size(); m++) {
      estimatedAmbiguities[m] = x.get(4 + m);
      estimatedAmbiguitiesCovariance[m] = covariance.get(4 + m, 4 + m);
    }
  
    if (init) {
      for (int i = 0; i < satAmb.size(); i++) {
        // Estimated ambiguity
        KFstate.set(i3 + satAmb.get(i), 0, estimatedAmbiguities[i]);
  
        // Store the variance of the estimated ambiguity
        Cee.set(i3 + satAmb.get(i), i3 + satAmb.get(i), estimatedAmbiguitiesCovariance[i]);
      }
    } else {
      for (int i = 0; i < satAmb.size(); i++) {
        // Estimated ambiguity
        KFprediction.set(i3 + satAmb.get(i), 0, estimatedAmbiguities[i]);
  
        // Store the variance of the estimated ambiguity
        Cvv.set(i3 + satAmb.get(i), i3 + satAmb.get(i), Math.pow( stDevAmbiguity, 2));
      }
    }
  }

  /**
   * @param roverObs
   * @param masterObs
   * @param masterPos
   */
  @Override
  void checkSatelliteConfiguration(Observations roverObs, Observations masterObs, Coordinates masterPos) {

    // Lists for keeping track of satellites that need ambiguity (re-)estimation
    ArrayList<Integer> newSatellites = new ArrayList<Integer>(0);
    ArrayList<Integer> slippedSatellites = new ArrayList<Integer>(0);

    // Check if satellites were lost since the previous epoch
    for (int i = 0; i < satOld.size(); i++) {

      // Set ambiguity of lost satellites to zero
//      if (!sats.gnssAvailPhase.contains(satOld.get(i))) {
      if (!sats.availPhase.contains(satOld.get(i)) && sats.typeAvailPhase.contains(satOld.get(i))) {

        if(goGPS.isDebug()) System.out.println("Lost satellite "+satOld.get(i));

        KFprediction.set(i3 + satOld.get(i), 0, 0);
      }
    }

    // Check if new satellites are available since the previous epoch
    for (int i = 0; i < sats.pos.length; i++) {

      if (sats.pos[i] != null && sats.availPhase.contains(sats.pos[i].getSatID()) && sats.typeAvailPhase.contains(sats.pos[i].getSatType())
          && !satOld.contains(sats.pos[i].getSatID()) && satTypeOld.contains(sats.pos[i].getSatType())) {

        newSatellites.add(sats.pos[i].getSatID());
        if(goGPS.isDebug()) System.out.println("New satellite "+sats.pos[i].getSatID());
      }
    }

    for (int i = 0; i < roverObs.getNumSat(); i++) {

      int satID = roverObs.getSatID(i);
      char satType = roverObs.getGnssType(i);
      String checkAvailGnss = String.valueOf(satType) + String.valueOf(satID);

      boolean lossOfLockCycleSlipRover;
      if (sats.gnssAvailPhase.contains(checkAvailGnss)) {
        // cycle slip detected by loss of lock indicator (disabled)
        lossOfLockCycleSlipRover = roverObs.getSatByIDType(satID, satType).isPossibleCycleSlip(goGPS.getFreq());
        lossOfLockCycleSlipRover = false;

        // cycle slip detected by Doppler predicted phase range
        boolean dopplerCycleSlipRover;
        if (goGPS.getCycleSlipDetectionStrategy() == GoGPS.CycleSlipDetectionStrategy.DOPPLER_PREDICTED_PHASE_RANGE) {
          dopplerCycleSlipRover = rover.getDopplerPredictedPhase(satID) != 0.0 && (Math.abs(roverObs.getSatByIDType(satID, satType).getPhaseCycles(goGPS.getFreq())
              - rover.getDopplerPredictedPhase(satID)) > goGPS.getCycleSlipThreshold());
        } else {
          dopplerCycleSlipRover = false;
        }

        // Rover-satellite observed pseudorange
        double roverSatCodeObs = roverObs.getSatByIDType(satID, satType).getCodeC(goGPS.getFreq());

        // Rover-satellite observed phase
        double roverSatPhaseObs = roverObs.getSatByIDType(satID, satType).getPhaserange(goGPS.getFreq());

        // Store estimated ambiguity combinations and their covariance
        double estimatedAmbiguity = (roverSatCodeObs - roverSatPhaseObs - 2*rover.satIonoCorr[i]) / roverObs.getSatByIDType(satID, satType).getWavelength(goGPS.getFreq());

        boolean obsCodeCycleSlip = Math.abs(KFprediction.get(i3+satID) - estimatedAmbiguity) > goGPS.getCycleSlipThreshold();

        boolean cycleSlip = (lossOfLockCycleSlipRover || dopplerCycleSlipRover || obsCodeCycleSlip);

        if (!newSatellites.contains(satID) && cycleSlip) {

          slippedSatellites.add(satID);

          if (dopplerCycleSlipRover)
            if(goGPS.isDebug()) System.out.println("[ROVER] Cycle slip on satellite "+satID+" (range diff = "+Math.abs(roverObs.getSatByIDType(satID, satType).getPhaseCycles(goGPS.getFreq())
                - rover.getDopplerPredictedPhase(satID))+")");
        }
      }
    }

    // Ambiguity estimation
    if (newSatellites.size() != 0 || slippedSatellites.size() != 0) {
      // List of satellites that need ambiguity estimation
      ArrayList<Integer> satAmb = newSatellites;
      satAmb.addAll(slippedSatellites);
      estimateAmbiguities( roverObs, null, null, satAmb, 0, false);
    }
  }

  /**
   * Run kalman filter on code and phase standalone.
   */
  public static void run( GoGPS goGPS ) {
    
    RoverPosition rover   = goGPS.getRoverPos();
    MasterPosition master = goGPS.getMasterPos();
    Satellites sats       = goGPS.getSats();
    ObservationsProducer roverIn = goGPS.getRoverIn();
    ObservationsProducer masterIn = goGPS.getMasterIn();
    boolean debug = goGPS.isDebug();

    long timeRead = System.currentTimeMillis();
    long depRead = 0;

    long timeProc = 0;
    long depProc = 0;

    KalmanFilter kf = new KF_SA_code_phase(goGPS);
    
    // Flag to check if Kalman filter has been initialized
    boolean kalmanInitialized = false;

    try {
      boolean validPosition = false;

      timeRead = System.currentTimeMillis() - timeRead;
      depRead = depRead + timeRead;

      Observations obsR = roverIn.getNextObservations();

      while (obsR != null) {

        if(debug)System.out.println("R:"+obsR.getRefTime().getMsec());

        timeRead = System.currentTimeMillis();
        depRead = depRead + timeRead;
        timeProc = System.currentTimeMillis();

        // If Kalman filter was not initialized and if there are at least four satellites
        boolean valid = true;
        if (!kalmanInitialized && obsR.getNumSat() >= 4) {
        	
         if( roverIn.getDefinedPosition() != null )
        	   roverIn.getDefinedPosition().cloneInto(rover);

          // Compute approximate positioning by iterative least-squares
          for (int iter = 0; iter < 3; iter++) {
            // Select all satellites
            sats.selectStandalone( obsR, -100 );
            
            if (sats.getAvailNumber() >= 4) {
              kf.codeStandalone( obsR, false, true);
            }
          }

          // If an approximate position was computed
          if (rover.isValidXYZ()) {

            // Initialize Kalman filter
            kf.init( obsR, null, roverIn.getDefinedPosition());

            if (rover.isValidXYZ()) {
              kalmanInitialized = true;
              if(debug)System.out.println("Kalman filter initialized.");
            } else {
              if(debug)System.out.println("Kalman filter not initialized.");
            }
          }else{
            if(debug)System.out.println("A-priori position (from code observations) is not valid.");
          }
        } else if (kalmanInitialized) {

          // Do a Kalman filter loop
          try{
            kf.loop( obsR, null, roverIn.getDefinedPosition());
          }catch(Exception e){
            e.printStackTrace();
            valid = false;
          }
        }

        timeProc = System.currentTimeMillis() - timeProc;
        depProc = depProc + timeProc;

        if(kalmanInitialized && valid){
          if(!validPosition){
            goGPS.notifyPositionConsumerEvent(PositionConsumer.EVENT_START_OF_TRACK);
            validPosition = true;
          }else
            if(goGPS.getPositionConsumers().size()>0){
              RoverPosition coord = new RoverPosition(rover, DopType.KALMAN, rover.getKpDop(), rover.getKhDop(), rover.getKvDop());
              coord.setRefTime(new Time(obsR.getRefTime().getMsec()));
              coord.obs = obsR;
              coord.sampleTime = obsR.getRefTime();
              coord.status = rover.status;
              goGPS.notifyPositionConsumerAddCoordinate(coord);
            }

        }
        //System.out.println("--------------------");

        if(debug)System.out.println("-- Get next epoch ---------------------------------------------------");
        // get next epoch
        obsR = roverIn.getNextObservations();
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      goGPS.notifyPositionConsumerEvent(PositionConsumer.EVENT_END_OF_TRACK);
    }

    int elapsedTimeSec = (int) Math.floor(depRead / 1000);
    int elapsedTimeMillisec = (int) (depRead - elapsedTimeSec * 1000);
    if(debug)System.out.println("\nElapsed time (read): " + elapsedTimeSec
        + " seconds " + elapsedTimeMillisec + " milliseconds.");

    elapsedTimeSec = (int) Math.floor(depProc / 1000);
    elapsedTimeMillisec = (int) (depProc - elapsedTimeSec * 1000);
    if(debug)System.out.println("\nElapsed time (proc): " + elapsedTimeSec
        + " seconds " + elapsedTimeMillisec + " milliseconds.");
  }
  
}
