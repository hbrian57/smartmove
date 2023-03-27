///=================================================================================================
// Class ExtendedKalmanFilter
//      Originally written by Antoine GRENIER
//      Enhanced & Modified by Aravind RAMESH
//        Date :  11/08/2022
///=================================================================================================
/*
 * Copyright 2018(c) IFSTTAR - TeamGEOLOC
 *
 * This file is part of the GeolocPVT application.
 *
 * GeolocPVT is distributed as a free software in order to build a community of users, contributors,
 * developers who will contribute to the project and ensure the necessary means for its evolution.
 *
 * GeolocPVT is a free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version. Any modification of source code in this
 * LGPL software must also be published under the LGPL license.
 *
 * GeolocPVT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Lesser General Public License along with GeolocPVT.
 * If not, see <https://www.gnu.org/licenses/lgpl.txt/>.
 */
///=================================================================================================

package fr.ifsttar.geoloc.geoloclib.computations;

import android.util.Log;

import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import fr.ifsttar.geoloc.geoloclib.Coordinates;

//

/**
 * Class Extended Kalman Filter
 *
 * Function for computations of an EKF solution.
 *
 * Code inspired by the GNSS Compare "DynamicExtendedKalmanFilter" class.
 */
public class ExtendedKalmanFilter
{
    private SimpleMatrix xHat_pred;
    private SimpleMatrix x_meas;
    private SimpleMatrix P_pred;
    private SimpleMatrix P_meas;

    private SimpleMatrix F;
    private SimpleMatrix Q;

    /* Computed Statistics*/
    private SimpleMatrix residuals;
    private SimpleMatrix variances;
    private  double sigma0_2;

    //----------------------------------------------------------------------------------------------

    /**
     * Constructor.
     * @param F Transition matrix
     * @param Q System noise matrix
     * @param x0 Initial solution
     * @param P0 Initial variance-covariance matrix of parameters
     */
    public ExtendedKalmanFilter(SimpleMatrix F, SimpleMatrix Q, SimpleMatrix x0, SimpleMatrix P0)
    {
        this.F = F;
        this.Q = Q;

        this.xHat_pred = x0;

        this.P_pred = P0;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute position using a Kalman filter
     * @param H Design matrix
     * @param B Observation vector
     */
    public void computeSolution(SimpleMatrix H, SimpleMatrix B, SimpleMatrix R)
    {
        SimpleMatrix gamma;
        SimpleMatrix S;
        SimpleMatrix K;

        /* Building the innovation vector*/
        //SimpleMatrix yTilde = H.mult(X);
        //SimpleMatrix yHat = H.mult(xHat_pred);
        //gamma = B.minus(yHat);

        // TODO: 08/12/2022 why gamma  = B (ie Y), instead of Y-H.X (or B-A.X)? 
        gamma = B;
        S = (H.mult(P_pred).mult(H.transpose())).plus(R);

        /* Building the kalman gain*/
        try
        {
            K = P_pred.mult(H.transpose()).mult(S.invert());
        } catch (SingularMatrixException e)
        {
            Log.e("EKF: ", "Matrix is singular, Inversion not possible", e);
            return;
        }

        SimpleMatrix dX = K.mult(gamma);

        x_meas = xHat_pred.plus(dX);
        P_meas = (SimpleMatrix.identity(P_pred.numCols()).minus(K.mult(H))).mult(P_pred);

        variances = P_meas.diag();

        residuals = gamma.plus(H.mult(xHat_pred)).minus(H.mult(x_meas));

        sigma0_2 = residuals.transpose().mult(R.invert()).mult(residuals).get(0, 0) / (B.numRows() - x_meas.numRows());

        xHat_pred = F.mult(x_meas);
        P_pred = F.mult(P_meas.mult(F.transpose())).plus(Q);

    }

    //----------------------------------------------------------------------------------------------

    public SimpleMatrix getX_meas() {return x_meas;}

    //----------------------------------------------------------------------------------------------

    public SimpleMatrix getxHat_pred(){ return xHat_pred;}

    //----------------------------------------------------------------------------------------------

    public SimpleMatrix getP_pred() {return P_pred;}

    //----------------------------------------------------------------------------------------------

    public SimpleMatrix getP_meas() {
        return P_meas;
    }

    //----------------------------------------------------------------------------------------------

    public SimpleMatrix getResiduals() {
        return residuals;
    }

    //----------------------------------------------------------------------------------------------

    public double getSigma0_2(){return sigma0_2;}

    //----------------------------------------------------------------------------------------------

    public void setSigma0_2(double sigma0_2) {this.sigma0_2 = sigma0_2;}

    //----------------------------------------------------------------------------------------------
}
