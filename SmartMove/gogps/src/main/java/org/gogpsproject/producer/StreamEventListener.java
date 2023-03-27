/*
 * Copyright (c) 2010 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
 *
 * This file is part of goGPS Project (goGPS).
 *
 * goGPS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * goGPS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with goGPS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.gogpsproject.producer;

import org.gogpsproject.ephemeris.EphGps;
import org.gogpsproject.ephemeris.GNSSEphemeris;
import org.gogpsproject.ephemeris.GNSSEphemerisCorrections;
import org.gogpsproject.ephemeris.SatelliteCodeBiases;
import org.gogpsproject.positioning.Coordinates;
import org.gogpsproject.producer.parser.IonoGps;

/**
 * @author Cryms.com
 *
 */
public interface StreamEventListener {

	public void streamClosed();
	public void addObservations(Observations o);
	public void addIonospheric(IonoGps iono);
	public void addEphemeris(EphGps eph);

	public void addEphemeris(GNSSEphemeris eph);
	public void addEphemerisCorr(GNSSEphemerisCorrections ephCorr);

	public void addSatelliteCodeBiases(SatelliteCodeBiases _scb);

	public void setDefinedPosition(Coordinates definedPosition);
	
	public Observations getCurrentObservations();
	public void pointToNextObservations();

	public void onStreamReceived(String msg);
}
