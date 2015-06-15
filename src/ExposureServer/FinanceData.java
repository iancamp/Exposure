/**
 * @author Ian Campbell iancamp@udel.edu Linkedin.com/in/iancamp
 */

package ExposureServer;

public class FinanceData {

	private String company;
	private String sym;
	private float lastPrice;
	private String comp;
	
	public FinanceData()
	{
		company = null;
		sym = null;
		lastPrice = -1f;
		comp = "";
	}
	
	public FinanceData(String company, String sym, float lastPrice)
	{
		this.company = company;
		this.sym = sym;
		this.lastPrice = lastPrice;
		this.comp = "";
	}
	
	public String getCompetitor()
	{
		return comp;
	}
	
	public void setCompetitor(String comp)
	{
		this.comp = comp;
	}
	
	public String getCompany()
	{
		return company;
	}
	
	public void setCompany(String company)
	{
		this.company = company;
	}
	
	public String getSym()
	{
		return sym;
	}
	
	public void setSym(String sym)
	{
		this.sym = sym;
	}
	
	public float getLastPrice()
	{
		return lastPrice;
	}
	
	public void setLastPrice(float newPrice)
	{
		lastPrice = newPrice;
	}
	
	public String toString()
	{
		return 
				"Company: " + company + "" + 
				"Stock ticker: " + sym + "" + 
				"Last price: " + lastPrice + 
				"Top competitor: " + comp + "\n";
	}
	
}
