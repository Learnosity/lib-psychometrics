package com.itemanalysis.psychometrics.irt.estimation;

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.quadrature.ContinuousQuadratureRule;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

public class MyTest {
    @Test
    public void my_test() {
        System.out.println("LSAT7 data - mirt test 3PL");

        //Read file and create response vectors
//        ItemResponseVector[] responseData = readItemResponseVectors("/testdata/lsat7-expanded.txt");
        ItemResponseVector[] responseData = readItemResponseVectors("/testdata/test.txt");
        int itemCount = responseData[0].nItems;

        //Create array of item response models
        ItemResponseModel[] irm = buildIRModel(itemCount);

        //computation of quadrature points as done in the mirt R package
        double quadPoints = 41;
        double min = -.8 * Math.sqrt(quadPoints);
        double max = -1 * min;
        ContinuousQuadratureRule latentDistribution = new ContinuousQuadratureRule((int) quadPoints, min, max, 0, 1);

        StartingValues startingValues = new StartingValues(responseData, irm);
        irm = startingValues.computeStartingValues();

        //estimate parameters
        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(responseData, irm, latentDistribution);
        DefaultEMStatusListener emStatus = new DefaultEMStatusListener();
        mmle.addEMStatusListener(emStatus);
        mmle.setVerbose(true);
        mmle.estimateParameters(1e-4, 500);
        mmle.computeItemStandardErrors();

        System.out.println();
        System.out.println(mmle.printItemParameters());
        mmle.computeG2ItemFit(10, itemCount);
//        mmle.computeSX2ItemFit(1);
//        System.out.println(mmle.printItemFitStatistics());

//        for (int j = 0; j < 5; j++) {
////            assertEquals("  LSAT 7 discrimination test", mirtDiscrimination[j], Precision.round(irm[j].getDiscrimination(), 2), 1e-1);
////            assertEquals("  LSAT 7 difficulty test", mirtDifficulty[j], Precision.round(irm[j].getDifficulty(),2), 1e-1);
////            assertEquals("  LSAT 7 guessing test", mirtGuessing[j], Precision.round(irm[j].getGuessing(),2), 1e-1);
//        }
    }

    private ItemResponseModel[] buildIRModel(int itemCount) {
        ItemResponseModel[] irm = new ItemResponseModel[itemCount];
        for (int j = 0; j < itemCount; j++) {
            Irm3PL pl3 = new Irm3PL(1.0, 0.0, 0.0, 1.0); //3PL - note that initial guessing parameter should not be zero
            pl3.setName(new VariableName("item" + (j + 1)));
            pl3.setDiscriminationPrior(new ItemParamPriorBeta4(1.75, 3.0, 0.0, 3.0));
            pl3.setDifficultyPrior(new ItemParamPriorBeta4(1.01, 1.01, -6.0, 6.0));
            pl3.setGuessingPrior(new ItemParamPriorBeta4(2.0, 4.0, 0.0, 1.0));
            irm[j] = pl3;
        }
        return irm;
    }

    private ItemResponseVector[] readItemResponseVectors(String dir) {
        ItemResponseFileSummary fileSummary = new ItemResponseFileSummary();
        File f = FileUtils.toFile(this.getClass().getResource(dir));
        return fileSummary.getResponseVectors(f, true);
    }
}
