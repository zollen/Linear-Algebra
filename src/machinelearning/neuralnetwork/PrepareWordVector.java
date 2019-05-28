package machinelearning.neuralnetwork;


import java.io.File;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

/**-
 * This program generates a word-vector from news items stored in resources folder.
 * News File is located at \dl4j-examples\src\main\resources\NewsData\RawNewsToGenerateWordVector.txt
 * Word vector file : \dl4j-examples\src\main\resources\NewsData\NewsWordVector.txt
 * Note :
 * 1) This code is modification of original example named Word2VecRawTextExample.java
 * 2) Word vector generated in this program is used in Training RNN to categorise news headlines.
 * <p>
 * <b></b>KIT Solutions Pvt. Ltd. (www.kitsol.com)</b>
 */
public class PrepareWordVector {


    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

        // Gets Path to Text file
        String classPathResource = new File("data/newsdata").getAbsolutePath() + File.separator;
        String filePath = new File(classPathResource + File.separator + "RawNewsToGenerateWordVector.txt").getAbsolutePath();

        System.out.println("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(filePath);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        //CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
        //So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
        //Additionally it forces lower case for all tokens.
        t.setTokenPreProcessor(new CommonPreprocessor());

        System.out.println("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
            .minWordFrequency(2)
            .iterations(5)
            .layerSize(100)
            .seed(42)
            .windowSize(20)
            .iterate(iter)
            .tokenizerFactory(t)
            .build();

        System.out.println("Fitting Word2Vec model....");
        vec.fit();

        System.out.println("Writing word vectors to text file....");

        // Write word vectors to file
        WordVectorSerializer.writeWordVectors(vec.lookupTable(), classPathResource + "NewsWordVector.txt");
    }
}