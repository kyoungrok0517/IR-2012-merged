import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.zzihee.ir.Lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;

import com.joogle.model.TermRankingFunction;
import com.joogle.model.TermWithWeight;
import com.joogle.model.YahooAnswer;
import com.joogle.model.YahooQuestion;
import com.joogle.utility.TermRankingHelper;
import com.joogle.utility.Tokenizer;
import com.joogle.utility.YahooAnswerHelper;

public class TREC {
	private static final String htmlRgx = "<(/)?([a-zA-Z0-9]*)(\\s[a-zA-Z0-9]*=[^>]*)?(\\s)*(/)?>";
	private static final String myHtmlRgx = "<[^>]+>";
	// private static final String commentRgx = "(?m)(?s)<!--(.*)-->";
	private static final String commentRgx = "(?m)(?s)<!(.*)(!)?>";

	private static List<String> stopwords;
	private static Map<String, Integer> corpus_vector;

	public static void main(String args[]) throws Exception {
		// String tmp =
		// "<Head> <Created on 3/15/96><body background=\"images/cdp_bkg.gif\" TEXT=\"#000000\"><center><img src=\"images/in_head.jpg\"></center><Title>Your Company on the Internet</Title></Head>"+
		// "<center><font size=+3><b>Everything you need to do business on the Internet...</font></b></center><img src=\"images/rainbar.gif\">"
		// +
		// "<FONT SIZE=\"5\" color=\"#01116F\"><B>";
		// String tmp2 =
		// "<HTML><HEAD><title>San Diego, CA Business, Services, & Entertainment Guide</title>";
		// String tmp3 = "<!=dd merong -->";
		// System.out.println(tmp.replaceAll(commentRgx, ""));
		// System.out.println(tmp.replaceAll(htmlRgx, ""));
		// System.out.println(tmp3.replaceAll(myHtmlRgx, ""));

		TREC trec = new TREC();

		System.out.println("Populating stopwords");
		stopwords = populateStopWords("./rsc/english_stopword_v2.txt");
		System.out.println("done.");
		
		System.out.println("Populating Corpus");
		corpus_vector = populateCorpusVector("./corpus/corpus_vector.txt");
		System.out.println("done.");
		
		for (String term : corpus_vector.keySet()) {
			System.out.println(term + ":" + corpus_vector.get(term));
		}

		// trec.indexDoc("E:/석사2_1/IR/proj/WT10G/", "dat/trec_index_stem_stop");
//		trec.search("dat/trec_index_stem_stop");
	}

	private static Map<String, Integer> populateCorpusVector(String filename) {
		BufferedReader reader = null;
		Map<String, Integer> vector = new HashMap<String, Integer>();

		try {
			reader = new BufferedReader(new FileReader(filename));

			while (true) {
				String line = reader.readLine();
				
				if (line == null) {
					break;
				}
				
				String[] pair = line.split(":");
				String term = pair[0];
				Integer count = Integer.valueOf(pair[1]);
				
				vector.put(term, count);
			}
		} catch (IOException e) {

		}

		return vector;
	}

	public void indexDoc(String dirIn, String dirOut) throws Exception {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("dat/log_stem_stop.txt")));

		// 진행 정보 출력하기
		// ZOut.println("[START] indexDoc() -- " +
		// ZDate.getNow("hh:ii:ss.lll"));

		// Lucene 초기화하기
		Lucene oLucene = new Lucene();
		oLucene.initEnglishAnalyzer("rsc/english_stopword_v2.txt");
		oLucene.setIndexDirectory(dirOut);
		IndexWriter oWriter = oLucene.getIndexWriter(OpenMode.CREATE);

		// 입출력 준비하기
		File f_dirIn = new File(dirIn);
		File f_dirList[] = f_dirIn.listFiles();
		for (File dir : f_dirList) {
			File f_fileList[];
			if (dir.isDirectory())
				f_fileList = dir.listFiles();
			else
				continue;

			for (File f : f_fileList) {
				if (f.toString().contains(".")) // 압축파일
					continue;

				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(f)));
				boolean isDoc = false;
				// boolean isHeader = false;
				boolean isContinue = false;
				// boolean isTitle = false;
				String s_docNO = "";
				// StringBuffer sb_title = new StringBuffer();
				StringBuffer sb_body = new StringBuffer();
				String s_continue = "";
				int i_bodyIndex = -1;

				while (true) {
					String s_line = br.readLine();

					if (s_line == null)
						break;

					if (s_line.equals(""))
						continue;

					// TREC header 처리
					if (isDoc && s_line.contains("</DOC>")) {
						isDoc = false;

						// indexing
						Document oDoc = new Document();
						oDoc.add(new Field("docNO", s_docNO, Field.Store.YES,
								Field.Index.NO, Field.TermVector.NO));
						// oDoc.add(new Field("title", sb_title.toString(),
						// Field.Store.YES, Field.Index.ANALYZED,
						// Field.TermVector.NO));
						oDoc.add(new Field("body", sb_body.toString(),
								Field.Store.YES, Field.Index.ANALYZED,
								Field.TermVector.NO));

						// IndexWriter에 Document 추가하기
						oWriter.addDocument(oDoc);

						System.out.println(s_docNO + ": " + sb_body.toString());
						bw.write(f.toString() + " " + s_docNO + ": "
								+ sb_body.toString() + "\n");
					}
					if (s_line.contains("<DOC>")) {
						// 변수 초기화
						s_docNO = "";
						// sb_title = new StringBuffer();
						sb_body = new StringBuffer();

						isDoc = true;
						isContinue = false;
						s_continue = "";
						i_bodyIndex = -1;
					}
					if (s_line.contains("<DOCNO>"))
						s_docNO = s_line.substring(s_line.indexOf("WTX"),
								s_line.indexOf("</DOCNO>")).trim();
					// if (s_line.startsWith("<DOCOLDNO>"))
					// ; // do nothing
					// if (s_line.contains("<DOCHDR>"))
					// isHeader = true;
					if (s_line.contains("</DOCHDR>")) {
						// isHeader = false;
						i_bodyIndex = s_line.indexOf("</DOCHDR>") + 9;
						if (i_bodyIndex == s_line.length()) {
							i_bodyIndex = 0;
							continue;
						}
					}

					// HTML
					if (i_bodyIndex != -1) // doc header 인덱싱 안함
					{
						// // title 처리
						// if (s_line.contains("<title>"))
						// {
						// if (s_line.contains("</title>"))
						// sb_title =
						// sb_title.append(s_line.substring(s_line.indexOf("<title>")
						// + 7, s_line.indexOf("</title>")).trim());
						// else
						// {
						// isTitle = true;
						// if (s_line.length() > 7)
						// sb_title =
						// sb_title.append(s_line.substring(s_line.indexOf("<title>")
						// + 7, s_line.length()).trim());
						// }
						// }
						// else if (isTitle) // 여러 줄에 걸친 title
						// {
						// if (s_line.contains("</title>"))
						// {
						// isTitle = false;
						// sb_title =
						// sb_title.append(s_line.substring(s_line.indexOf("</title>")).trim());
						// }
						// else
						// sb_title = sb_title.append(s_line.trim());
						// }

						String s_tmp;

						if (isContinue)
							s_tmp = s_continue + " "
									+ s_line.substring(i_bodyIndex);
						else
							s_tmp = s_line.substring(i_bodyIndex);

						String s_noTag = s_tmp.replaceAll(myHtmlRgx, "");
						// s_noTag = s_noTag.replaceAll(commentRgx, "");
						// s_noTag = s_noTag.replaceAll(commentRgx, "");
						// s_noTag = s_noTag.replaceAll("&quot;", "");
						// s_noTag = s_noTag.replaceAll("&apos;", "");
						// s_noTag = s_noTag.replaceAll("&amp;", "");
						// s_noTag = s_noTag.replaceAll("&lt;", "");
						// s_noTag = s_noTag.replaceAll("&gt;", "");
						// s_noTag = s_noTag.replaceAll("&nbsp;", "");

						if (s_noTag.contains("<")) // 여러 줄을 걸친 태그
						{
							isContinue = true;
							s_continue = s_noTag;
						} else {
							isContinue = false;
							s_continue = "";

							if (!s_noTag.equals(""))
								sb_body = sb_body.append(s_noTag.trim() + " ");
						}
					}
				}
			}
		}

		oWriter.optimize();
		oWriter.close();

		bw.close();

		System.out.println("Complete");
	}

	private static String getNormalizedQuery(String query,
			List<String> stopwords) {
		List<String> query_tokens = Tokenizer.tokenize(query);
		String query_modified = "";

		for (String term : query_tokens) {
			if (!stopwords.contains(term)) {
				query_modified += " " + term;
			}
		}

		return query_modified;
	}

	private static List<String> populateStopWords(String filename) {
		BufferedReader reader = null;
		List<String> result = new ArrayList<String>();

		try {
			reader = new BufferedReader(new FileReader(filename));

			while (true) {
				String stopword = reader.readLine();

				if (stopword != null) {
					result.add(stopword);
				} else {
					break;
				}
			}
		} catch (IOException e) {

		}

		return result;
	}

	private final int TERM_EXPANSION_LIMIT = 10;

	private String getExpandedQuery(String query,
			TermRankingFunction function_type) {
		String normalized_query = getNormalizedQuery(query, stopwords);

		System.out.println("Normalized Query: " + normalized_query);

		System.out.println("Fetching and processing Yahoo data...");
		List<YahooQuestion> questions = YahooAnswerHelper
				.searchQuestions(normalized_query);

		// build the collection & PRF documents
		// Collection: retrieved
		List<String> collection = new ArrayList<String>();
		List<String> prf_docs = new ArrayList<String>();
		for (YahooQuestion question : questions) {
			String question_content = question.Content;
			String chosen_answer_content = question.ChosenAnswer;
			List<YahooAnswer> answers = YahooAnswerHelper.getAnswers(question);

			prf_docs.add(chosen_answer_content);
			collection.add(question_content);
			collection.add(chosen_answer_content);
			for (YahooAnswer ans : answers) {
				collection.add(ans.Content);
			}
		}

		Map<String, Double> weight_vector = new HashMap<String, Double>();
		TermRankingHelper rank_helper = new TermRankingHelper(prf_docs,
				collection, stopwords);

		for (String doc : prf_docs) {
			List<String> vector = TermRankingHelper.getUniqueTermVector(doc);

			for (String t : vector) {
				if (!stopwords.contains(t)) {
					double weight = 0.0;

					// get weight using specified ranking function
					switch (function_type) {
					case ROCCHIO:
						weight = rank_helper.getRocchioWeight(t);
						break;
					case RSV:
						weight = rank_helper.getRSVWeight(t);
						break;
					case CHI:
						weight = rank_helper.getChiSquareWeight(t);
						break;
					}

					weight_vector.put(t, weight);
				}

			}

		}

		List<TermWithWeight> tww_list = new ArrayList<TermWithWeight>();
		for (String t : weight_vector.keySet()) {
			tww_list.add(new TermWithWeight(t, weight_vector.get(t)));
		}

		Collections.sort(tww_list);

		String expanded_query = normalized_query + " ";

		for (int i = 0; i < tww_list.size(); i++) {
			if (i >= TERM_EXPANSION_LIMIT) {
				break;
			}

			String term = tww_list.get(i).term;
			expanded_query += term + " ";
		}

		System.out.println("Done");

		return expanded_query;
	}

	public void search(String dirIdx) throws Exception {
		Scanner in = new Scanner(System.in);

		while (true) {

			String sQuery = in.nextLine();
			Lucene oLucene = new Lucene();

			String expaned_query = getExpandedQuery(sQuery,
					TermRankingFunction.ROCCHIO);

			System.out.println("Expanded Query: " + expaned_query);

			// oLucene.setIndexDirectory(dirIdx);
			// oLucene.initEnglishAnalyzer("rsc/english_stopword_v2.txt");
			// ZRecordList lstResDoc = oLucene.retrieveDocuments(sQuery, true,
			// 10,
			// "body", ZRecord.byObj("docNO", "body"));
			//
			// // System.out.println(lstResDoc);
			// ZOut.println(" ==> Number of Results = " + lstResDoc.size());
			// ZOut.println(lstResDoc.size() > 0 ? lstResDoc.toString("\n",
			// "\t")
			// : "No result");
			// ZOut.println("\n");
		}
	}
}
