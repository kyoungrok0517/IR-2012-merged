/**
 * 
 */

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import net.zzihee.io.*;
import net.zzihee.lang.*;
import net.zzihee.util.ZCnt;
import net.zzihee.util.ZDate;

/**
 * class of Lucene searcher adapter
 * 
 *    1. Document indexing (List --> Directory)
 *    2. Document searching (Query --> List of searched documents)
 *    
 * @author  Jihee Ryu
 * @since   2010/05/25
 * @version 2011/05/05
 */
public class Lucene {
	
	protected Analyzer oAnalyzer;
	protected String dirIndex;

	/**
	 * Main function
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		// Lucene 초기화하기
		Lucene oLucene = new Lucene();
		oLucene.initEnglishAnalyzer("rsc/english_stopword_v2.txt");

		// 검색용 데이터 만들기
		ZRecordList lstDoc = ZRecordList.byNone();
		lstDoc.addValueAsObj("Apple", "Apple Company in United States (US)");
		lstDoc.addValueAsObj("Google", "Google Company in United States (US)");
		lstDoc.addValueAsObj("Microsoft", "Microsoft (MS) Company in United States (US)");
		lstDoc.addValueAsObj("Harvard", "Harvard University in United States (US)");
		lstDoc.addValueAsObj("MIT", "Massachusetts Institute of Technology (MIT) in United States (US)");
		lstDoc.addValueAsObj("Stanford", "Stanford University in United States (US)");
		lstDoc.addValueAsObj("Cambridge", "Cambridge University in United Kingdom (UK)");
		lstDoc.addValueAsObj("Oxford", "Oxford University in United Kingdom (UK)");
		lstDoc.addValueAsObj("KAIST", "Korea Advanced Institute of Science and Technology (KAIST) in South Korea");
		lstDoc.addValueAsObj("Seoul National University", "Seoul National University (SNU) in South Korea");
		lstDoc.addValueAsObj("POSTECH", "Pohang University of Science and Technology (POSTECH) in South Korea");
		
		// Lucene 색인하기
		String dirIdx = "C:/_lucene_test_idx/";
		oLucene.setIndexDirectory(dirIdx);
		oLucene.indexDocumentsFromList(lstDoc, OpenMode.CREATE, 0, 1, true);
		
		// Lucene 검색하기
		{
			String sQuery = Lucene.stripEscape("CNU Korea");
			ZOut.println("Query : "+sQuery);
	
			ZRecordList lstResDoc = oLucene.retrieveDocuments(sQuery, false, 100);
			ZOut.println(" ==> Number of Results = "+lstResDoc.size());
			if(lstResDoc.size()>0)
				ZOut.println(lstResDoc.toString("\n", "\t"));
		}

		// Lucene 추가 색인하기
		oLucene.indexAdditionalDocument("Chungnam National University", "Chungnam National University (CNU) in South Korea");
		
		// Lucene 검색하기
		{
			String sQuery = Lucene.stripEscape("CNU Korea");
			ZOut.println("Query : "+sQuery);
	
			ZRecordList lstResDoc = oLucene.retrieveDocuments(sQuery, false, 100);
			ZOut.println(" ==> Number of Results = "+lstResDoc.size());
			if(lstResDoc.size()>0)
				ZOut.println(lstResDoc.toString("\n", "\t"));
		}
	}
	
	/**
	 * 
	 * @param sQuery
	 * @return
	 */
	public static String stripEscape(String sQuery) {
		
		return QueryParser.escape(sQuery);
	}

	/**
	 * 
	 */
	public void initStandardAnalyzer() {
		
		// Analyzer 초기화하기
		ZRecordList lstStopword = ZRecordSet.byNone();
		this.oAnalyzer = new StandardAnalyzer(Version.LUCENE_31, lstStopword.toSetAsObj());
	}
	
	/**
	 * 
	 * @param fileStopword
	 */
	public void initEnglishAnalyzer() {
		
		// Analyzer 초기화하기
		ZRecordList lstStopword = ZRecordSet.byNone();
		this.oAnalyzer = new EnglishAnalyzer(Version.LUCENE_31, lstStopword.toSetAsObj());
	}
	
	/**
	 * 
	 * @param fileStopword
	 */
	public void initEnglishAnalyzer(String fileStopword) {
		
		// Analyzer 초기화하기
		ZRecordList lstStopword = ZRecordSet.byList(ZFile.readLines(fileStopword));
		this.oAnalyzer = new EnglishAnalyzer(Version.LUCENE_31, lstStopword.toSetAsObj());
	}
	
	/**
	 * 
	 * @param fileStopword
	 * @param fileStemExclusion
	 */
	public void initEnglishAnalyzer(String fileStopword, String fileStemExclusion) {
		
		// Analyzer 초기화하기
		ZRecordList lstStopword = ZRecordSet.byList(ZFile.readLines(fileStopword));
		ZRecordList lstStemExclusion = ZRecordSet.byList(ZFile.readLines(fileStemExclusion));
		this.oAnalyzer = new EnglishAnalyzer(Version.LUCENE_31, lstStopword.toSetAsObj(), lstStemExclusion.toSetAsObj());
	}

	/**
	 * 
	 * @param dirIdx
	 */
	public void setIndexDirectory(String dirIdx) {
		
		this.dirIndex = ZFile.normalizeDir(dirIdx);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getIndexDirectory() {
		
		return this.dirIndex;
	}

	/**
	 * 
	 * @param dirIdx
	 * @return
	 * @throws Exception
	 */
	public IndexWriter getIndexWriter(OpenMode cMode) throws Exception {
		
		// IndexWriter 객체 생성하기
		IndexWriterConfig oConf = (new IndexWriterConfig(Version.LUCENE_31, this.oAnalyzer)).setOpenMode(cMode);
		IndexWriter oWriter = new IndexWriter(FSDirectory.open(new File(this.dirIndex)), oConf);
	
		return oWriter;
	}

	/**
	 * 
	 * @param lstDoc
	 * @param nColName
	 * @param nColCont
	 * @param bTrace
	 * @throws Exception
	 */
	public void indexDocumentsFromList(ZRecordList lstDoc, OpenMode cMode, int nColName, int nColCont, boolean bTrace) throws Exception {
		
		// 진행 정보 출력하기
		if(bTrace)
			ZOut.println("[START] indexDocumentsFromList() -- " + ZDate.getNow("hh:ii:ss.lll"));
		
		// IndexWriter 객체 생성하기
		IndexWriter oWriter = this.getIndexWriter(cMode);
		
		// 각 문서별 처리하기
		ZCnt cntDoc = new ZCnt();
		for(int i=0; i<lstDoc.size(); i++) {
			
			// 문서 인식하기
			String sName = lstDoc.getValueAsStr(i, nColName);
			String sCont = lstDoc.getValueAsStr(i, nColCont);
			
			// 진행 정보 출력하기
			if(bTrace)
				ZOut.sprintln("  + [Doc:%d] List of documents : %s", cntDoc.getCountWithClick(), sName);
	
			// Document 객체 생성하기
			Document oDoc = new Document();
			oDoc.add(new Field("name", sName, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
			oDoc.add(new Field("cont", sCont, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
			
			// IndexWriter에 Document 추가하기
			oWriter.addDocument(oDoc);
		}
		
		// IndexWriter 최적화하기
		oWriter.optimize();
	
		// IndexWriter 객체 닫기
		oWriter.close();
		
		// 진행 정보 출력하기
		if(bTrace)
			ZOut.println("[STOP] indexDocumentsFromList() -- " + ZDate.getNow("hh:ii:ss.lll"));
	}
	
	/**
	 * 
	 * @param fileDoc
	 * @param nColName
	 * @param nColCont
	 * @param bTrace
	 * @throws Exception
	 */
	public void indexDocumentsFromFile(String fileDoc, OpenMode cMode, int nColName, int nColCont, boolean bTrace) throws Exception {
		
		// 진행 정보 출력하기
		if(bTrace)
			ZOut.println("[START] indexDocumentsFromFile() -- " + ZDate.getNow("hh:ii:ss.lll"));
		
		// IndexWriter 객체 생성하기
		IndexWriter oWriter = this.getIndexWriter(cMode);
		
		// 입출력 준비하기
		ZFile zfInp = new ZFile(fileDoc, ZFile.TO_READ);
		
		// 각 문서별 처리하기
		ZCnt cntDoc = new ZCnt();
		while(true) {
	
			// 문서 인식하기
			String sLine = zfInp.readLine();
			if(sLine == null)
				break;
			String[] arrCol = sLine.split("\t");
			String sName = arrCol[nColName];
			String sCont = arrCol[nColCont];
	
			// 진행 정보 출력하기
			if(bTrace)
				ZOut.sprintln("  + [Doc:%d] %s : %s", cntDoc.getCountWithClick(), fileDoc, sName);

			// Document 객체 생성하기
			Document oDoc = new Document();
			oDoc.add(new Field("name", sName, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
			oDoc.add(new Field("cont", sCont, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
			
			// IndexWriter에 Document 추가하기
			oWriter.addDocument(oDoc);
		}
		
		// 입력 파일 닫기
		zfInp.close();
		
		// IndexWriter 최적화하기
		oWriter.optimize();
	
		// IndexWriter 객체 닫기
		oWriter.close();
		
		// 진행 정보 출력하기
		if(bTrace)
			ZOut.println("[STOP] indexDocumentsFromFile() -- " + ZDate.getNow("hh:ii:ss.lll"));
	}
	
	/**
	 * 
	 * @param dirDoc
	 * @param bTrace
	 * @throws Exception
	 */
	public void indexDocumentsFromDir(String dirDoc, OpenMode cMode, boolean bTrace) throws Exception {
		
		// 진행 정보 출력하기
		if(bTrace)
			ZOut.println("[START] indexDocumentsFromDir() -- " + ZDate.getNow("hh:ii:ss.lll"));
	
		// IndexWriter 객체 생성하기
		IndexWriter oWriter = this.getIndexWriter(cMode);
		
		// 각 문서별 처리하기
		ZCnt cntDoc = new ZCnt();
		ZRecordList lstDocFile = ZFile.listDir(dirDoc);
		for(int i=0; i<lstDocFile.size(); i++) {
			
			// 문서 인식하기
			String fileDoc = lstDocFile.getValueAsStr(i);
			String sName = ZFile.filebody(fileDoc);
			String sCont = ZFile.readAll(fileDoc);
			
			// 진행 정보 출력하기
			if(bTrace)
				ZOut.sprintln("  + [Doc:%d] %s : %s", cntDoc.getCountWithClick(), fileDoc, sName);

			// Document 객체 생성하기
			Document oDoc = new Document();
			oDoc.add(new Field("name", sName, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
			oDoc.add(new Field("cont", sCont, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
			
			// IndexWriter에 Document 추가하기
			oWriter.addDocument(oDoc);
		}
		
		// IndexWriter 최적화하기
		oWriter.optimize();
	
		// IndexWriter 객체 닫기
		oWriter.close();
		
		// 진행 정보 출력하기
		if(bTrace)
			ZOut.println("[STOP] indexDocumentsFromDir() -- " + ZDate.getNow("hh:ii:ss.lll"));
	}
	
	/**
	 * 
	 * @param sName
	 * @param sCont
	 * @throws Exception
	 */
	public void indexAdditionalDocument(String sName, String sCont) throws Exception {
		
		// IndexWriter 객체 생성하기
		IndexWriterConfig oConf = (new IndexWriterConfig(Version.LUCENE_31, this.oAnalyzer)).setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter oWriter = new IndexWriter(FSDirectory.open(new File(this.dirIndex)), oConf);
		
		// Document 객체 생성하기
		Document oDoc = new Document();
		oDoc.add(new Field("name", sName, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO));
		oDoc.add(new Field("cont", sCont, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
		
		// IndexWriter에 Document 추가하기
		oWriter.addDocument(oDoc);
		
		// IndexWriter 최적화하기
		oWriter.optimize();
		
		// IndexWriter 객체 닫기
		oWriter.close();
	}

	/**
	 * 
	 * @param sQuery
	 * @param bDisjunct
	 * @param numCnd
	 * @return
	 * @throws Exception
	 */
	public ZRecordList retrieveDocuments(String sQuery, boolean bDisjunct, int numCnd) throws Exception {
		
		return this.retrieveDocuments(sQuery, bDisjunct, numCnd, "cont", ZRecord.byObj("name", "cont"));
	}
	
	/**
	 * 
	 * @param sQuery
	 * @param bDisjunct
	 * @param numCnd
	 * @param sSearchField
	 * @param recResultField
	 * @return
	 * @throws Exception
	 */
	public ZRecordList retrieveDocuments(String sQuery, boolean bDisjunct, int numCnd, String sSearchField, ZRecord recResultField) throws Exception {
		
		if(!ZFile.exists(this.dirIndex))
			return null;

		// Query 객체 생성하기
		QueryParser oParser = new QueryParser(Version.LUCENE_31, sSearchField, this.oAnalyzer);
		
		if(!bDisjunct)
			oParser.setDefaultOperator(Operator.AND);
		Query oQuery = oParser.parse(sQuery);
		
		// IndexSearcher 객체 생성하기
		IndexSearcher oSearcher = new IndexSearcher(FSDirectory.open(new File(this.dirIndex)));
		
		// 문서 검색하기
		TopDocs oTopDocs = oSearcher.search(oQuery, oSearcher.maxDoc());
		
		// ScoreDoc별 처리하기
		ZRecordList lstResDoc = ZRecordList.byNone();
		for(int i=0; i<Math.min(numCnd, oTopDocs.scoreDocs.length); i++) {
	
			// ScoreDoc 인식하기
			ScoreDoc oScoreDoc = oTopDocs.scoreDocs[i];
	
			// Document 인식하기
			Document oFoundDoc = oSearcher.doc(oScoreDoc.doc);
			ZRecord recResult = ZRecord.byNone();
			for(int j=0; j<recResultField.size(); j++) {
				String sResult = oFoundDoc.get(recResultField.getValueAsStr(j));
				recResult.addValue(sResult);
			}
			float fScore = oScoreDoc.score;
			
			// 결과 리스트에 추가하기
			lstResDoc.addValueAsObj(fScore, recResult);
		}
		
		return lstResDoc;
	}
	
public ZRecordList retrieveDocuments(String sQuery, boolean bDisjunct, String sSearchField, ZRecord recResultField) throws Exception {
		
		if(!ZFile.exists(this.dirIndex))
			return null;

		// Query 객체 생성하기
		QueryParser oParser = new QueryParser(Version.LUCENE_31, sSearchField, this.oAnalyzer);
		
		if(!bDisjunct)
			oParser.setDefaultOperator(Operator.AND);
		Query oQuery = oParser.parse(sQuery);
		
		// IndexSearcher 객체 생성하기
		IndexSearcher oSearcher = new IndexSearcher(FSDirectory.open(new File(this.dirIndex)));
		
		// 문서 검색하기
		TopDocs oTopDocs = oSearcher.search(oQuery, oSearcher.maxDoc());
		
		// ScoreDoc별 처리하기
		ZRecordList lstResDoc = ZRecordList.byNone();
		for(int i=0; i<oTopDocs.scoreDocs.length; i++) {
	
			// ScoreDoc 인식하기
			ScoreDoc oScoreDoc = oTopDocs.scoreDocs[i];
	
			// Document 인식하기
			Document oFoundDoc = oSearcher.doc(oScoreDoc.doc);
			ZRecord recResult = ZRecord.byNone();
			for(int j=0; j<recResultField.size(); j++) {
				String sResult = oFoundDoc.get(recResultField.getValueAsStr(j));
				recResult.addValue(sResult);
			}
			float fScore = oScoreDoc.score;
			
			// 결과 리스트에 추가하기
			lstResDoc.addValueAsObj(fScore, recResult);
		}
		
		return lstResDoc;
	}
}
