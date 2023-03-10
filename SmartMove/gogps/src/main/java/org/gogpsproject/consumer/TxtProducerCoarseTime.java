/*
 * Copyright (c) 2011 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
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
package org.gogpsproject.consumer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.gogpsproject.Status;
import org.gogpsproject.positioning.RoverPosition;
/**
 * <p>
 * Produces TXT file
 * </p>
 *
 * @author Eugenio Realini
 */

public class TxtProducerCoarseTime implements PositionConsumer  {

	private static DecimalFormat f = new DecimalFormat("0.000");
	private static DecimalFormat g = new DecimalFormat("0.00000000");

	private SimpleDateFormat dateTXT = new SimpleDateFormat("yy/MM/dd");
	private SimpleDateFormat timeTXT = new SimpleDateFormat("HH:mm:ss.SSS");

	private String filename = null;
	private boolean debug=false;

	private final static TimeZone TZ = TimeZone.getTimeZone("GMT");
	FileWriter out;
	
	public TxtProducerCoarseTime(String filename) throws IOException{
		this.filename = filename;

		writeHeader();
		
		dateTXT.setTimeZone(TZ);
		timeTXT.setTimeZone(TZ);
	}

	/* (non-Javadoc)
   * @see org.gogpsproject.producer.PositionConsumer#event(int)
   */
  @Override
  public void event(int event) {
    switch( event ){
  	  case EVENT_START_OF_TRACK:
        out = writeHeader();
      break;
  	  case EVENT_END_OF_TRACK: 
      break;
    }
  }

  /* (non-Javadoc)
   * @see org.gogpsproject.producer.PositionConsumer#startOfTrack()
   */
  public FileWriter writeHeader() {
    try {
      FileWriter out = new FileWriter(filename);
  
      out.write("Index         Status   Sats      Date      RTC time       FIX time     cErr " +
//            "GPS week        GPS tow" +
            "     Latitude    Longitude     Altitude    HDOP   eRes" +
            "   cErrRate" +
            "\r\n");
      out.flush();
      return out;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /* (non-Javadoc)
	 * @see org.gogpsproject.producer.PositionConsumer#addCoordinate(org.gogpsproject.Coordinates)
	 */
	@Override
	public void addCoordinate(RoverPosition c ) {
		if(debug) System.out.println("Lon:"+g.format(c.getGeodeticLongitude()) + " " // geod.get(0)
				+"Lat:"+ g.format(c.getGeodeticLatitude()) + " " // geod.get(1)
				+"H:"+ f.format(c.getGeodeticHeight()) + "\t" // geod.get(2)
				+"P:"+ c.getpDop()+" "
				+"H:"+ c.gethDop()+" "
				+"V:"+ c.getvDop()+" ");//geod.get(2)

    try {
      PrintWriter pw = new PrintWriter(out);

      pw.printf("%5d  ", c.obs.index);
      pw.printf("%13s  ", c.status.toString() );
      pw.printf("%2d/%2d  ", c.satsInUse, c.obs.getNumSat() );
      
      // RTC date, time
      String d  = dateTXT.format(new Date(c.getRefTime().getMsec()));
      String t0 = timeTXT.format(new Date(c.sampleTime.getMsec()));

      pw.printf("%8s%14s", d, t0 );

      if( c.status != Status.Valid ){
        pw.printf("\r\n");
        out.flush();
        return;
      }

      // FIX time
      String t  = timeTXT.format(new Date(c.getRefTime().getMsec()));

      double delta = (c.getRefTime().getMsec() - c.sampleTime.getMsec())/1000.0;
      pw.printf("%15s%10.3f", t, delta);

//      //GPS week
//      int week = c.getRefTime().getGpsWeek();
//      pw.printf("%12d", week);
      
//      //GPS time-of-week (tow)
//      double tow = c.getRefTime().getGpsTime();
//      pw.printf("%15.3f", tow);
      
      //latitude, longitude, ellipsoidal height
      double lat = c.getGeodeticLatitude();
      double lon = c.getGeodeticLongitude();
      double hEllips = c.getGeodeticHeight();
      
      pw.printf("%13.5f%13.5f%13.5f", lat, lon, hEllips);
      
      pw.printf("%8.1f", c.gethDop() );

      pw.printf("%7.1f", c.eRes );

      pw.printf("%9.0f", c.clockErrorRate );

      pw.printf("\r\n");
      out.flush();

    } catch (NullPointerException e) {
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
	}

  /**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

}
