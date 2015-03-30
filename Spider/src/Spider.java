import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 抓取知乎上的问题
 */
public class Spider {
	private static final String ZHIHU_URL = "http://www.zhihu.com/question/";
	
	/*
	 * 知乎发现（http://www.zhihu.com/explore）上推荐的问题
	 * <a class="question_link" target="_blank" href="/question/28473597/answer/41324984">
	 * 匹配 /question/28473597/answer/41324984
	 */
	private static final String QUESTION_URL = "<h2>.+?question_link.+?href=\"(.+?)\".+?</h2>";

	//匹配问题title
	private static final String QUESTION_TITLE = "zh-question-title.+?<h2.+?>(.+?)</h2>";
	//匹配问题描述
	private static final String QUESTION_DETAIL = "zh-question-detail.+?<div.+?>(.*?)</div>";
	
	//获取问题id
	private static final String REAL_QUESTION_URL = "question/(.*?)/answer";
	
	private static final Pattern patternTitle = Pattern.compile(QUESTION_TITLE);
	private static final Pattern patternDetail = Pattern.compile(QUESTION_DETAIL);
	private static final Pattern patternRealUrl = Pattern.compile(REAL_QUESTION_URL);
	
	public static String getHtml(String url) {
		StringBuilder result = new StringBuilder();
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	public static ArrayList<Zhihu> getZhihuList(String content) {
		ArrayList<Zhihu> results = new ArrayList<Zhihu>();
		Pattern pattern = Pattern.compile(QUESTION_URL);
		Matcher matcher = pattern.matcher(content);
		String url = null;
		while (matcher.find()) {
		   //确保不出现 /question/28071260/answer/42649016这样的url
			url = getQuestionUrl(matcher.group(1));			
			Zhihu zhihu = getZhihu(url);
			//markdown 格式
			System.out.print(" - ");
			System.out.println("[" + zhihu.getQuestionTitle() + "](" + zhihu.getQuestionUrl() + ")");
			System.out.println();
			System.out.println(zhihu.getQuestionDetail());
			System.out.println();
			results.add(zhihu);
		}
		return results;
	}
	
	/*
	 * url为问题url，如：
	 * http://www.zhihu.com/question/20191417 
	 */
	public static Zhihu getZhihu(String questionUrl){
		String page = getHtml(questionUrl);
		Matcher matcher = patternTitle.matcher(page);
		String questionTitle = null;
		String questionDetail = null;
		if(matcher.find()){
			questionTitle = matcher.group(1);
		}
		matcher = patternDetail.matcher(page);
		if(matcher.find()){
			questionDetail = matcher.group(1);
			questionDetail = questionDetail.replaceAll("<br>", "\r\n"); 
			questionDetail = questionDetail.replaceAll("<.*?>", ""); //加问号表示尽可能少地匹配
		}
		
		Zhihu zhihu = new Zhihu();
		zhihu.setQuestionUrl(questionUrl);
		zhihu.setQuestionTitle(questionTitle);
		zhihu.setQuestionDetail(questionDetail);
		
		return zhihu;
	}
	
	public static String getQuestionUrl(String url){
		Matcher matcher = patternRealUrl.matcher(url);
		if(matcher.find()){
			return ZHIHU_URL + matcher.group(1);
		}
		return url;
	}
	
	public static void main(String[] args) {
		String url = "http://www.zhihu.com/explore";
		String content = Spider.getHtml(url);
		Spider.getZhihuList(content);
	}
}