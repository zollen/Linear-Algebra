package machinelearning.neuralnetwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class TestNews {
	
	private static WordVectors wordVectors;
	private static TokenizerFactory tokenizerFactory;

	private static MultiLayerNetwork net;

	public static void main(String args[]) throws Exception {

		tokenizerFactory = new DefaultTokenizerFactory();
		tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
		net = MultiLayerNetwork.load(new File("data/newsdata/NewsModel.net"), true);
		wordVectors = WordVectorSerializer.readWord2VecModel(new File("data/newsdata/NewsWordVector.txt"));
		
		eval("Murder in Elm Street!");
	}

	public static void eval(String msg) {
		DataSet testNews = prepareTestData(msg);
		INDArray fet = testNews.getFeatures();
		INDArray predicted = net.output(fet, false);
		long[] arrsiz = predicted.shape();

		File categories = new File("data/newsdata/LabelledNews/categories.txt");

		double max = 0;
		int pos = 0;
		for (int i = 0; i < arrsiz[1]; i++) {
			if (max < (double) predicted.getColumn(i).sumNumber()) {
				max = (double) predicted.getColumn(i).sumNumber();
				pos = i;
			}
		}

		try (BufferedReader brCategories = new BufferedReader(new FileReader(categories))) {
			String temp = "";
			List<String> labels = new ArrayList<>();
			while ((temp = brCategories.readLine()) != null) {
				labels.add(temp);
			}
			brCategories.close();

			System.out.println("CATEGORY: " + labels.get(pos).split(",")[1]);
		} catch (Exception e) {
			System.out.println("File Exception : " + e.getMessage());
		}
	}

	private static DataSet prepareTestData(String i_news) {
		List<String> news = new ArrayList<>(1);
		int[] category = new int[1];
		news.add(i_news);

		List<List<String>> allTokens = new ArrayList<>(news.size());
		int maxLength = 0;
		for (String s : news) {
			List<String> tokens = tokenizerFactory.create(s).getTokens();
			List<String> tokensFiltered = new ArrayList<>();
			for (String t : tokens) {
				if (wordVectors.hasWord(t))
					tokensFiltered.add(t);
			}
			allTokens.add(tokensFiltered);
			maxLength = Math.max(maxLength, tokensFiltered.size());
		}

		INDArray features = Nd4j.create(news.size(), wordVectors.lookupTable().layerSize(), maxLength);
		INDArray labels = Nd4j.create(news.size(), 3, maxLength); // labels: Crime, Politics, Bollywood,
																	// Business&Development
		INDArray featuresMask = Nd4j.zeros(news.size(), maxLength);
		INDArray labelsMask = Nd4j.zeros(news.size(), maxLength);

		int[] temp = new int[2];
		for (int i = 0; i < news.size(); i++) {
			List<String> tokens = allTokens.get(i);
			temp[0] = i;
			for (int j = 0; j < tokens.size() && j < maxLength; j++) {
				String token = tokens.get(j);
				INDArray vector = wordVectors.getWordVectorMatrix(token);
				features.put(new INDArrayIndex[] { NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.point(j) },
						vector);

				temp[1] = j;
				featuresMask.putScalar(temp, 1.0);
			}
			int idx = category[i];
			int lastIdx = Math.min(tokens.size(), maxLength);
			labels.putScalar(new int[] { i, idx, lastIdx - 1 }, 1.0);
			labelsMask.putScalar(new int[] { i, lastIdx - 1 }, 1.0);
		}

		DataSet ds = new DataSet(features, labels, featuresMask, labelsMask);
		return ds;
	}
}