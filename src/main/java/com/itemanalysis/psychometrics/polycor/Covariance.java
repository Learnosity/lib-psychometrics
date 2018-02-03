/*
 * Copyright 2012 J. Patrick Meyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.psychometrics.polycor;


import org.apache.commons.math3.distribution.TDistribution;

/**
 * 
 * @author J. Patrick Meyer
 */
public class Covariance implements Comparable<Covariance>{

	private double deltaX = 0.0;
    private double deltaY = 0.0;
    private double meanX = 0.0;
    private double meanY = 0.0;
	private double N=0;
	private Double covarianceNumerator=0.0;
    private double varNumeratorX = 0.0;
    private double varNumeratorY = 0.0;
    private double covariance = 0;
    boolean fixedValue = false;
    boolean unbiased = true;

    public Covariance(boolean unbiased){
        this.unbiased = unbiased;
    }

	public Covariance(){
        this(true);
	}

    public Covariance(double covariance, boolean unbiased){
        this.covariance = covariance;
        this.unbiased = unbiased;
        fixedValue = true;
    }

    public Covariance(double covariance){
        this(covariance, true);
    }

	public Covariance(Covariance cov, boolean unbiased){
        this.unbiased = unbiased;
        this.N = cov.N;
        this.deltaX = cov.deltaX;
        this.deltaY = cov.deltaY;
        this.meanX = cov.meanX;
        this.meanY = cov.meanY;
        this.covarianceNumerator = cov.covarianceNumerator;
        this.varNumeratorX = cov.varNumeratorX;
        this.varNumeratorY = cov.varNumeratorY;
	}

    public Covariance(Covariance cov){
	    this(cov, true);
    }

    /**
     * Update formula for recursive on-line (i.e. one pass) method of computing the covariance.This method
     * is given by XXXX. It is an extension of the on-line mean and variance algorithms by Knuth (1998) and
     * Welford (1962). It is more numerically accurate than the computational formula (i.e. naive) for
     * the covariance. The standard deviation algorithms are also more numerically accurate than the
     * computational formula (i.e. naive).
     *
     * Donald E. Knuth (1998). The Art of Computer Programming, volume 2: Seminumerical Algorithms, 3rd edn., p. 232. Boston: Addison-Wesley.
     * B. P. Welford (1962)."Note on a method for calculating corrected sums of squares and products". Technometrics 4(3):419–420.
     *
     * @param x
     * @param y
     */
    public void increment(Double x, Double y){
        if(x!=null & y!=null){
            N++;
            deltaX = x - meanX;
            deltaY = y - meanY;
            meanX += deltaX/N;
            meanY += deltaY/N;
            covarianceNumerator += ((N-1.0)/N)*deltaX*deltaY;
            varNumeratorX += deltaX*(x-meanX);
            varNumeratorY += deltaY*(y-meanY);
        }
        
    }

    public double value(){
        if(fixedValue) return covariance;
        if(N<1) return Double.NaN;
        if(unbiased){
            return covarianceNumerator/(N-1.0);
        }else{
            return covarianceNumerator/N;
        }
    }

//    public Double value(){
//        return value();
//    }
    
    public double varX(){
        if(N<1) return Double.NaN;
        if(unbiased){
            return varNumeratorX/(N-1.0);
        }else{
            return varNumeratorX/N;
        }
    }

    public double sdX(){
        return Math.sqrt(varX());
    }
    
    public double varY(){
        if(N<1) return Double.NaN;
        if(unbiased){
            return varNumeratorY/(N-1.0);
        }else{
            return varNumeratorY/N;
        }
    }

    public double sdY(){
        return Math.sqrt(varY());
    }

    public double correlation(){
        double cv = this.value();
        double r = cv/(Math.sqrt(this.varX())*Math.sqrt(this.varY()));
        return r;
    }

//    public double correlation(){
//        return correlation(true);
//    }

    public double correlationStandardError(){
        if(N<3) return Double.NaN;
        double r = correlation();
        double r2 = Math.pow(r,2);
        double se = Math.sqrt((1-r2)/(N-2.0));
        return se;
    }

    public double correlationPvalue(){
        double se = correlationStandardError();
        if(se==0.0) return Double.NaN;
        double r = correlation();
        double tval = r/se;
        double df = N-2.0;
        TDistribution t = new TDistribution(df);
        double pvalue = 1-t.cumulativeProbability(tval);
        double twoSidedPvalue = 2.0*Math.min(pvalue, 1-pvalue);//from R function cor.test()
        return twoSidedPvalue;
    }

	public double sampleSize(){
		return N;
	}

	public int compareTo(Covariance that){
		if(this.value()>that.value()) return 1;
		if(this.value()<that.value()) return -1;
		return 0;
	}

	public boolean equals(Object obj){
		if(this==obj)return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		return ((Double)obj)==this.value();

	}

	public int hashCode(){
		return Double.valueOf(value()).hashCode();
	}

}
