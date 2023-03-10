///=================================================================================================
// Class PVT
//      Author :  Antoine GRENIER
//        Date :  2019/09/06
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

/**
 * Abstract class PVT
 *
 *  Functions for Least Square Estimation.
 */
public abstract class PVT
{
    protected SimpleMatrix N; // Also known as P^-1
    protected SimpleMatrix X;
    protected SimpleMatrix dX;
    protected SimpleMatrix Qx;

    protected SimpleMatrix prevN;
    protected SimpleMatrix prevX;

    protected double sigma0_2;

    //----------------------------------------------------------------------------------------------

    /**
     * LSE computations
     * @param A Design matrix
     * @param B Observation vector
     * @param Q Variance-covariance matrix
     * @return Success
     */
    public boolean computeLSE(SimpleMatrix A, SimpleMatrix B, SimpleMatrix Q)
    {
        SimpleMatrix C;
        SimpleMatrix P = Q.invert();

        C = A.transpose().mult(P).mult(B);
        N = A.transpose().mult(P).mult(A);

        try
        {
            dX = N.invert().mult(C);
        }
        catch (Exception e)
        {
            Log.e("LSET: ", "Matrix is singular, Inversion not possible", e);
            return false;
        }

        // TODO: 07/12/2022 never used...
        SimpleMatrix v = B.minus(A.mult(dX));

        sigma0_2 = v.transpose().mult(P).mult(v).get(0,0) / (B.numRows() - dX.numRows());

        X = X.plus(dX);

        // TODO: 07/12/2022 never used... 
        Qx = A.transpose().mult(P).mult(A).invert();

        return true;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * Compute recursive least square based on the previous computation.
     * Reference: Aided Navigation: GPS with High Rate Sensors, Jay A. Farrell
     * @param A Design matrix
     * @param Q Measurements weight
     */
    public void computeRecursiveLSE(SimpleMatrix A, SimpleMatrix Q)
    {
        if(X == null || N == null)
        {
            Log.e("LSE" , "Least square solution must have been computed once before recurssive LSE can be " +
                    "performed ");
            return;
        }

        SimpleMatrix yTilde = A.mult(X);
        SimpleMatrix yHat = A.mult(prevX);

        N = prevN.plus(A.transpose().mult(Q).mult(A));
        SimpleMatrix K = N.invert().mult(A.transpose().mult(Q));

        SimpleMatrix tmp = K.mult(yTilde.minus(yHat));

        X = prevX.plus(K.mult(yTilde.minus(yHat)));
    }

    //----------------------------------------------------------------------------------------------

    protected SimpleMatrix getN() {
        return N;
    }

    //---------------------------------------------------------------------------------------------

    protected void setN(SimpleMatrix n) {
        N = n;
    }

    //---------------------------------------------------------------------------------------------

    protected SimpleMatrix getX() {
        return X;
    }

    //---------------------------------------------------------------------------------------------

    protected void setX(SimpleMatrix x) {
        X = x;
    }

    //---------------------------------------------------------------------------------------------

    protected SimpleMatrix getPrevN() {
        return prevN;
    }

    //---------------------------------------------------------------------------------------------

    protected void setPrevN(SimpleMatrix prevN) {
        this.prevN = prevN;
    }

    //---------------------------------------------------------------------------------------------

    protected SimpleMatrix getPrevX() {
        return prevX;
    }

    //---------------------------------------------------------------------------------------------

    protected void setPrevX(SimpleMatrix prevX) {
        this.prevX = prevX;
    }

    //----------------------------------------------------------------------------------------------

    public SimpleMatrix getQx() {
        return Qx;
    }

    //----------------------------------------------------------------------------------------------

    public double getSigma0_2(){return sigma0_2;}

    //----------------------------------------------------------------------------------------------

    public void setSigma0_2(double sigma0_2) {this.sigma0_2 = sigma0_2;}
}
