//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.30 at 05:41:29 PM CEST 
//


package es.ehubio.proteomics.waters.plgs;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the es.ehubio.proteomics.waters.plgs package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: es.ehubio.proteomics.waters.plgs
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Result }
     * 
     */
    public Result createRESULT() {
        return new Result();
    }

    /**
     * Create an instance of {@link Result.Hit }
     * 
     */
    public Result.Hit createRESULTHIT() {
        return new Result.Hit();
    }

    /**
     * Create an instance of {@link Result.Hit.Protein }
     * 
     */
    public Result.Hit.Protein createRESULTHITPROTEIN() {
        return new Result.Hit.Protein();
    }

    /**
     * Create an instance of {@link Result.Hit.Protein.SEQUENCEMATCH }
     * 
     */
    public Result.Hit.Protein.SEQUENCEMATCH createRESULTHITPROTEINSEQUENCEMATCH() {
        return new Result.Hit.Protein.SEQUENCEMATCH();
    }

    /**
     * Create an instance of {@link Result.AnalysisModifier }
     * 
     */
    public Result.AnalysisModifier createRESULTANALYSISMODIFIER() {
        return new Result.AnalysisModifier();
    }

    /**
     * Create an instance of {@link Result.AnalysisModifier.Modifier }
     * 
     */
    public Result.AnalysisModifier.Modifier createRESULTANALYSISMODIFIERMODIFIER() {
        return new Result.AnalysisModifier.Modifier();
    }

    /**
     * Create an instance of {@link Result.Calculated }
     * 
     */
    public Result.Calculated createRESULTCALCULATED() {
        return new Result.Calculated();
    }

    /**
     * Create an instance of {@link Result.Params }
     * 
     */
    public Result.Params createRESULTPARAMS() {
        return new Result.Params();
    }

    /**
     * Create an instance of {@link Result.GeneratedBy }
     * 
     */
    public Result.GeneratedBy createRESULTGeneratedBy() {
        return new Result.GeneratedBy();
    }

    /**
     * Create an instance of {@link Result.Product }
     * 
     */
    public Result.Product createRESULTPRODUCT() {
        return new Result.Product();
    }

    /**
     * Create an instance of {@link Result.Peptide }
     * 
     */
    public Result.Peptide createRESULTPEPTIDE() {
        return new Result.Peptide();
    }

    /**
     * Create an instance of {@link Result.QueryMass }
     * 
     */
    public Result.QueryMass createRESULTQUERYMASS() {
        return new Result.QueryMass();
    }

    /**
     * Create an instance of {@link Result.Hit.Protein.SEQUENCEMATCH.FRAGMENTION }
     * 
     */
    public Result.Hit.Protein.SEQUENCEMATCH.FRAGMENTION createRESULTHITPROTEINSEQUENCEMATCHFRAGMENTION() {
        return new Result.Hit.Protein.SEQUENCEMATCH.FRAGMENTION();
    }

    /**
     * Create an instance of {@link Result.AnalysisModifier.Modifier.MODIFIES }
     * 
     */
    public Result.AnalysisModifier.Modifier.MODIFIES createRESULTANALYSISMODIFIERMODIFIERMODIFIES() {
        return new Result.AnalysisModifier.Modifier.MODIFIES();
    }

    /**
     * Create an instance of {@link Result.Calculated.OUTPUT }
     * 
     */
    public Result.Calculated.OUTPUT createRESULTCALCULATEDOUTPUT() {
        return new Result.Calculated.OUTPUT();
    }

    /**
     * Create an instance of {@link Result.Params.PARAM }
     * 
     */
    public Result.Params.PARAM createRESULTPARAMSPARAM() {
        return new Result.Params.PARAM();
    }

}