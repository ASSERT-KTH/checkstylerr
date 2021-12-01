package com.ec.survey.controller;

import com.ec.survey.tools.Tools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/utils")
public class UtilsController extends BasicController {
	
	public @Value("${captcha.bypass:@null}") String bypassCaptcha;
	
	@RequestMapping(value = "/euCountries", method = {RequestMethod.GET, RequestMethod.HEAD})
	public @ResponseBody Map<String, String> getListOfCountries(HttpServletRequest request) {
		Locale locale = new Locale(request.getParameter("lang"));
		
		Map<String, String> countryList = new HashMap<>();

		countryList.put("AT", resources.getMessage("label.country.Austria", null, locale));
		countryList.put("BE", resources.getMessage("label.country.Belgium", null, locale));
		countryList.put("BG", resources.getMessage("label.country.Bulgaria", null, locale));
		countryList.put("CY", resources.getMessage("label.country.Cyprus", null, locale));
		countryList.put("CZ", resources.getMessage("label.un.Czechia", null, locale));
		countryList.put("DE", resources.getMessage("label.country.Germany", null, locale));
		countryList.put("DK", resources.getMessage("label.country.Denmark", null, locale));
		countryList.put("EE", resources.getMessage("label.country.Estonia", null, locale));
		countryList.put("EL", resources.getMessage("label.country.Greece", null, locale));
		countryList.put("ES", resources.getMessage("label.country.Spain", null, locale));
		countryList.put("FI", resources.getMessage("label.country.Finland", null, locale));
		countryList.put("FR", resources.getMessage("label.country.France", null, locale));
		countryList.put("HU", resources.getMessage("label.country.Hungary", null, locale));
		countryList.put("HR", resources.getMessage("label.country.Croatia", null, locale));
		countryList.put("IE", resources.getMessage("label.country.Ireland", null, locale));
		countryList.put("IR", resources.getMessage("label.country.Italy", null, locale));
		countryList.put("LT", resources.getMessage("label.country.Lithuania", null, locale));
		countryList.put("LU", resources.getMessage("label.country.Luxembourg", null, locale));
		countryList.put("LV", resources.getMessage("label.country.Latvia", null, locale));
		countryList.put("MT", resources.getMessage("label.country.Malta", null, locale));
		countryList.put("NL", resources.getMessage("label.country.Netherlands", null, locale));
		countryList.put("PL", resources.getMessage("label.country.Poland", null, locale));
		countryList.put("PT", resources.getMessage("label.country.Portugal", null, locale));
		countryList.put("RO", resources.getMessage("label.country.Romania", null, locale));
		countryList.put("SE", resources.getMessage("label.country.Sweden", null, locale));
		countryList.put("SI", resources.getMessage("label.country.Slovenia", null, locale));
		countryList.put("SK", resources.getMessage("label.country.SlovakRepublic", null, locale));
		countryList.put("UK", resources.getMessage("label.country.UnitedKingdom", null, locale));
		
		countryList = Tools.sortByComparator(countryList);
		return countryList;
	}
	
	@RequestMapping(value = "/euLanguages", method = {RequestMethod.GET, RequestMethod.HEAD})
	public @ResponseBody Map<String, String> getListOfLanguages(HttpServletRequest request) {
		Locale locale = new Locale(request.getParameter("lang"));
		
		Map<String, String> langList = new HashMap<>();

		langList.put("BG", resources.getMessage("label.lang.Bulgarian", null, locale));
		langList.put("CS", resources.getMessage("label.lang.Czech", null, locale));
		langList.put("DA", resources.getMessage("label.lang.Danish", null, locale));
		langList.put("DE", resources.getMessage("label.lang.German", null, locale));
		langList.put("EL", resources.getMessage("label.lang.Greek", null, locale));
		langList.put("EN", resources.getMessage("label.lang.English", null, locale));
		langList.put("ES", resources.getMessage("label.lang.Spanish", null, locale));
		langList.put("ET", resources.getMessage("label.lang.Estonian", null, locale));
		langList.put("FI", resources.getMessage("label.lang.Finnish", null, locale));
		langList.put("FR", resources.getMessage("label.lang.French", null, locale));
		langList.put("GA", resources.getMessage("label.lang.Gaelic", null, locale));
		langList.put("HR", resources.getMessage("label.lang.Croatian", null, locale));
		langList.put("HU", resources.getMessage("label.lang.Hungarian", null, locale));
		langList.put("IT", resources.getMessage("label.lang.Italian", null, locale));
		langList.put("LT", resources.getMessage("label.lang.Lithuanian", null, locale));
		langList.put("LV", resources.getMessage("label.lang.Latvian", null, locale));
		langList.put("MT", resources.getMessage("label.lang.Maltese", null, locale));
		langList.put("NL", resources.getMessage("label.lang.Dutch", null, locale));
		langList.put("PL", resources.getMessage("label.lang.Polish", null, locale));
		langList.put("PT", resources.getMessage("label.lang.Portuguese", null, locale));
		langList.put("RO", resources.getMessage("label.lang.Romanian", null, locale));
		langList.put("SK", resources.getMessage("label.lang.Slovak", null, locale));
		langList.put("SL", resources.getMessage("label.lang.Slovenian", null, locale));
		langList.put("SV", resources.getMessage("label.lang.Swedish", null, locale));	
		
		langList = Tools.sortByComparator(langList);
		return langList;
	}
	
	@RequestMapping(value = "/unCountries", method = {RequestMethod.GET, RequestMethod.HEAD})
	public @ResponseBody Map<String, String> getListOfUNCountries(HttpServletRequest request) {
		Locale locale = new Locale(request.getParameter("lang"));
	
		Map<String, String> countryList = new HashMap<>();
	
		countryList.put("AF", resources.getMessage("label.un.Afghanistan", null, locale));
		countryList.put("AL", resources.getMessage("label.un.Albania", null, locale));
		countryList.put("DZ", resources.getMessage("label.un.Algeria", null, locale));
		countryList.put("AD", resources.getMessage("label.un.Andorra", null, locale));
		countryList.put("AO", resources.getMessage("label.un.Angola", null, locale));
		countryList.put("AG", resources.getMessage("label.un.AntiguaandBarbuda", null, locale));
		countryList.put("AR", resources.getMessage("label.un.Argentina", null, locale));
		countryList.put("AM", resources.getMessage("label.un.Armenia", null, locale));
		countryList.put("AU", resources.getMessage("label.un.Australia", null, locale));
		countryList.put("AT", resources.getMessage("label.un.Austria", null, locale));
		countryList.put("AZ", resources.getMessage("label.un.Azerbaijan", null, locale));
		countryList.put("BS", resources.getMessage("label.un.Bahamas", null, locale));
		countryList.put("BH", resources.getMessage("label.un.Bahrain", null, locale));
		countryList.put("BD", resources.getMessage("label.un.Bangladesh", null, locale));
		countryList.put("BB", resources.getMessage("label.un.Barbados", null, locale));
		countryList.put("BY", resources.getMessage("label.un.Belarus", null, locale));
		countryList.put("BE", resources.getMessage("label.un.Belgium", null, locale));
		countryList.put("BZ", resources.getMessage("label.un.Belize", null, locale));
		countryList.put("BJ", resources.getMessage("label.un.Benin", null, locale));
		countryList.put("BT", resources.getMessage("label.un.Bhutan", null, locale));
		countryList.put("BO", resources.getMessage("label.un.Bolivia", null, locale));
		countryList.put("BA", resources.getMessage("label.un.BosniaandHerzegovina", null, locale));
		countryList.put("BW", resources.getMessage("label.un.Botswana", null, locale));
		countryList.put("BR", resources.getMessage("label.un.Brazil", null, locale));
		countryList.put("BN", resources.getMessage("label.un.BruneiDarussalam", null, locale));
		countryList.put("BG", resources.getMessage("label.un.Bulgaria", null, locale));
		countryList.put("BF", resources.getMessage("label.un.BurkinaFaso", null, locale));
		countryList.put("BI", resources.getMessage("label.un.Burundi", null, locale));
		countryList.put("CV", resources.getMessage("label.un.CaboVerde", null, locale));
		countryList.put("KH", resources.getMessage("label.un.Cambodia", null, locale));
		countryList.put("CM", resources.getMessage("label.un.Cameroon", null, locale));
		countryList.put("CA", resources.getMessage("label.un.Canada", null, locale));
		countryList.put("CF", resources.getMessage("label.un.CentralAfricanRepublic", null, locale));
		countryList.put("TD", resources.getMessage("label.un.Chad", null, locale));
		countryList.put("CL", resources.getMessage("label.un.Chile", null, locale));
		countryList.put("CN", resources.getMessage("label.un.China", null, locale));
		countryList.put("CO", resources.getMessage("label.un.Colombia", null, locale));
		countryList.put("KM", resources.getMessage("label.un.Comoros", null, locale));
		countryList.put("CG", resources.getMessage("label.un.Congo", null, locale));
		countryList.put("CR", resources.getMessage("label.un.CostaRica", null, locale));
		countryList.put("CI", resources.getMessage("label.un.CoteDIvoire", null, locale));
		countryList.put("HR", resources.getMessage("label.un.Croatia", null, locale));
		countryList.put("CU", resources.getMessage("label.un.Cuba", null, locale));
		countryList.put("CY", resources.getMessage("label.un.Cyprus", null, locale));
		countryList.put("CZ", resources.getMessage("label.un.Czechia", null, locale));
		countryList.put("KP", resources.getMessage("label.un.NorthKorea", null, locale));
		countryList.put("CD", resources.getMessage("label.un.DemocraticRepublicoftheCongo", null, locale));
		countryList.put("DK", resources.getMessage("label.un.Denmark", null, locale));
		countryList.put("DJ", resources.getMessage("label.un.Djibouti", null, locale));
		countryList.put("DM", resources.getMessage("label.un.Dominica", null, locale));
		countryList.put("DO", resources.getMessage("label.un.DominicanRepublic", null, locale));
		countryList.put("EC", resources.getMessage("label.un.Ecuador", null, locale));
		countryList.put("EG", resources.getMessage("label.un.Egypt", null, locale));
		countryList.put("SV", resources.getMessage("label.un.ElSalvador", null, locale));
		countryList.put("GQ", resources.getMessage("label.un.EquatorialGuinea", null, locale));
		countryList.put("ER", resources.getMessage("label.un.Eritrea", null, locale));
		countryList.put("EE", resources.getMessage("label.un.Estonia", null, locale));
		countryList.put("ET", resources.getMessage("label.un.Ethiopia", null, locale));
		countryList.put("FJ", resources.getMessage("label.un.Fiji", null, locale));
		countryList.put("FI", resources.getMessage("label.un.Finland", null, locale));
		countryList.put("FR", resources.getMessage("label.un.France", null, locale));
		countryList.put("GA", resources.getMessage("label.un.Gabon", null, locale));
		countryList.put("GM", resources.getMessage("label.un.Gambia", null, locale));
		countryList.put("GE", resources.getMessage("label.un.Georgia", null, locale));
		countryList.put("DE", resources.getMessage("label.un.Germany", null, locale));
		countryList.put("GH", resources.getMessage("label.un.Ghana", null, locale));
		countryList.put("GR", resources.getMessage("label.un.Greece", null, locale));
		countryList.put("GD", resources.getMessage("label.un.Grenada", null, locale));
		countryList.put("GT", resources.getMessage("label.un.Guatemala", null, locale));
		countryList.put("GN", resources.getMessage("label.un.Guinea", null, locale));
		countryList.put("GW", resources.getMessage("label.un.GuineaBissau", null, locale));
		countryList.put("GY", resources.getMessage("label.un.Guyana", null, locale));
		countryList.put("HT", resources.getMessage("label.un.Haiti", null, locale));
		countryList.put("HN", resources.getMessage("label.un.Honduras", null, locale));
		countryList.put("HU", resources.getMessage("label.un.Hungary", null, locale));
		countryList.put("IS", resources.getMessage("label.un.Iceland", null, locale));
		countryList.put("IN", resources.getMessage("label.un.India", null, locale));
		countryList.put("ID", resources.getMessage("label.un.Indonesia", null, locale));
		countryList.put("IR", resources.getMessage("label.un.Iran", null, locale));
		countryList.put("IQ", resources.getMessage("label.un.Iraq", null, locale));
		countryList.put("IE", resources.getMessage("label.un.Ireland", null, locale));
		countryList.put("IL", resources.getMessage("label.un.Israel", null, locale));
		countryList.put("IT", resources.getMessage("label.un.Italy", null, locale));
		countryList.put("JM", resources.getMessage("label.un.Jamaica", null, locale));
		countryList.put("JP", resources.getMessage("label.un.Japan", null, locale));
		countryList.put("JO", resources.getMessage("label.un.Jordan", null, locale));
		countryList.put("KZ", resources.getMessage("label.un.Kazakhstan", null, locale));
		countryList.put("KE", resources.getMessage("label.un.Kenya", null, locale));
		countryList.put("KI", resources.getMessage("label.un.Kiribati", null, locale));
		countryList.put("KW", resources.getMessage("label.un.Kuwait", null, locale));
		countryList.put("KG", resources.getMessage("label.un.Kyrgyzstan", null, locale));
		countryList.put("LA", resources.getMessage("label.un.Laos", null, locale));
		countryList.put("LV", resources.getMessage("label.un.Latvia", null, locale));
		countryList.put("LB", resources.getMessage("label.un.Lebanon", null, locale));
		countryList.put("LS", resources.getMessage("label.un.Lesotho", null, locale));
		countryList.put("LR", resources.getMessage("label.un.Liberia", null, locale));
		countryList.put("LY", resources.getMessage("label.un.Libya", null, locale));
		countryList.put("LI", resources.getMessage("label.un.Liechtenstein", null, locale));
		countryList.put("LT", resources.getMessage("label.un.Lithuania", null, locale));
		countryList.put("LU", resources.getMessage("label.un.Luxembourg", null, locale));
		countryList.put("MG", resources.getMessage("label.un.Madagascar", null, locale));
		countryList.put("MW", resources.getMessage("label.un.Malawi", null, locale));
		countryList.put("MY", resources.getMessage("label.un.Malaysia", null, locale));
		countryList.put("MV", resources.getMessage("label.un.Maldives", null, locale));
		countryList.put("ML", resources.getMessage("label.un.Mali", null, locale));
		countryList.put("MT", resources.getMessage("label.un.Malta", null, locale));
		countryList.put("MH", resources.getMessage("label.un.MarshallIslands", null, locale));
		countryList.put("MR", resources.getMessage("label.un.Mauritania", null, locale));
		countryList.put("MU", resources.getMessage("label.un.Mauritius", null, locale));
		countryList.put("MX", resources.getMessage("label.un.Mexico", null, locale));
		countryList.put("FM", resources.getMessage("label.un.Micronesia", null, locale));
		countryList.put("MC", resources.getMessage("label.un.Monaco", null, locale));
		countryList.put("MN", resources.getMessage("label.un.Mongolia", null, locale));
		countryList.put("ME", resources.getMessage("label.un.Montenegro", null, locale));
		countryList.put("MA", resources.getMessage("label.un.Morocco", null, locale));
		countryList.put("MZ", resources.getMessage("label.un.Mozambique", null, locale));
		countryList.put("MM", resources.getMessage("label.un.Myanmar", null, locale));
		countryList.put("NA", resources.getMessage("label.un.Namibia", null, locale));
		countryList.put("NR", resources.getMessage("label.un.Nauru", null, locale));
		countryList.put("NP", resources.getMessage("label.un.Nepal", null, locale));
		countryList.put("NL", resources.getMessage("label.un.Netherlands", null, locale));
		countryList.put("NZ", resources.getMessage("label.un.NewZealand", null, locale));
		countryList.put("NI", resources.getMessage("label.un.Nicaragua", null, locale));
		countryList.put("NE", resources.getMessage("label.un.Niger", null, locale));
		countryList.put("NG", resources.getMessage("label.un.Nigeria", null, locale));
		countryList.put("NO", resources.getMessage("label.un.Norway", null, locale));
		countryList.put("OM", resources.getMessage("label.un.Oman", null, locale));
		countryList.put("PK", resources.getMessage("label.un.Pakistan", null, locale));
		countryList.put("PW", resources.getMessage("label.un.Palau", null, locale));
		countryList.put("PA", resources.getMessage("label.un.Panama", null, locale));
		countryList.put("PG", resources.getMessage("label.un.PapuaNewGuinea", null, locale));
		countryList.put("PY", resources.getMessage("label.un.Paraguay", null, locale));
		countryList.put("PE", resources.getMessage("label.un.Peru", null, locale));
		countryList.put("PH", resources.getMessage("label.un.Philippines", null, locale));
		countryList.put("PL", resources.getMessage("label.un.Poland", null, locale));
		countryList.put("PT", resources.getMessage("label.un.Portugal", null, locale));
		countryList.put("QA", resources.getMessage("label.un.Qatar", null, locale));
		countryList.put("KR", resources.getMessage("label.un.SouthKorea", null, locale));
		countryList.put("MD", resources.getMessage("label.un.RepublicMoldova", null, locale));
		countryList.put("RO", resources.getMessage("label.un.Romania", null, locale));
		countryList.put("RU", resources.getMessage("label.un.RussianFederation", null, locale));
		countryList.put("RW", resources.getMessage("label.un.Rwanda", null, locale));
		countryList.put("KN", resources.getMessage("label.un.SaintKittsandNevis", null, locale));
		countryList.put("LC", resources.getMessage("label.un.SaintLucia", null, locale));
		countryList.put("VC", resources.getMessage("label.un.SaintVincentandtheGrenadines", null, locale));
		countryList.put("WS", resources.getMessage("label.un.Samoa", null, locale));
		countryList.put("SM", resources.getMessage("label.un.SanMarino", null, locale));
		countryList.put("ST", resources.getMessage("label.un.SaoTomeandPrincipe", null, locale));
		countryList.put("SA", resources.getMessage("label.un.SaudiArabia", null, locale));
		countryList.put("SN", resources.getMessage("label.un.Senegal", null, locale));
		countryList.put("RS", resources.getMessage("label.un.Serbia", null, locale));
		countryList.put("SC", resources.getMessage("label.un.Seychelles", null, locale));
		countryList.put("SL", resources.getMessage("label.un.SierraLeone", null, locale));
		countryList.put("SG", resources.getMessage("label.un.Singapore", null, locale));
		countryList.put("SK", resources.getMessage("label.un.Slovakia", null, locale));
		countryList.put("SI", resources.getMessage("label.un.Slovenia", null, locale));
		countryList.put("SB", resources.getMessage("label.un.SolomonIslands", null, locale));
		countryList.put("SO", resources.getMessage("label.un.Somalia", null, locale));
		countryList.put("ZA", resources.getMessage("label.un.SouthAfrica", null, locale));
		countryList.put("SS", resources.getMessage("label.un.SouthSudan", null, locale));
		countryList.put("ES", resources.getMessage("label.un.Spain", null, locale));
		countryList.put("LK", resources.getMessage("label.un.SriLanka", null, locale));
		countryList.put("SD", resources.getMessage("label.un.Sudan", null, locale));
		countryList.put("SR", resources.getMessage("label.un.Suriname", null, locale));
		countryList.put("SZ", resources.getMessage("label.un.Eswatini", null, locale));
		countryList.put("SE", resources.getMessage("label.un.Sweden", null, locale));
		countryList.put("CH", resources.getMessage("label.un.Switzerland", null, locale));
		countryList.put("SY", resources.getMessage("label.un.SyrianArabRepublic", null, locale));
		countryList.put("TJ", resources.getMessage("label.un.Tajikistan", null, locale));
		countryList.put("TH", resources.getMessage("label.un.Thailand", null, locale));
		countryList.put("MK", resources.getMessage("label.un.NorthMacedonia", null, locale));
		countryList.put("TL", resources.getMessage("label.un.Timor-Leste", null, locale));
		countryList.put("TG", resources.getMessage("label.un.Togo", null, locale));
		countryList.put("TO", resources.getMessage("label.un.Tonga", null, locale));
		countryList.put("TT", resources.getMessage("label.un.TrinidadandTobago", null, locale));
		countryList.put("TN", resources.getMessage("label.un.Tunisia", null, locale));
		countryList.put("TR", resources.getMessage("label.un.Turkey", null, locale));
		countryList.put("TM", resources.getMessage("label.un.Turkmenistan", null, locale));
		countryList.put("TV", resources.getMessage("label.un.Tuvalu", null, locale));
		countryList.put("UG", resources.getMessage("label.un.Uganda", null, locale));
		countryList.put("UA", resources.getMessage("label.un.Ukraine", null, locale));
		countryList.put("AE", resources.getMessage("label.un.UnitedArabEmirates", null, locale));
		countryList.put("GB", resources.getMessage("label.un.UnitedKingdom", null, locale));
		countryList.put("TZ", resources.getMessage("label.un.Tanzania", null, locale));
		countryList.put("US", resources.getMessage("label.un.UnitedStatesofAmerica", null, locale));
		countryList.put("UY", resources.getMessage("label.un.Uruguay", null, locale));
		countryList.put("UZ", resources.getMessage("label.un.Uzbekistan", null, locale));
		countryList.put("VU", resources.getMessage("label.un.Vanuatu", null, locale));
		countryList.put("VE", resources.getMessage("label.un.Venezuela", null, locale));
		countryList.put("VN", resources.getMessage("label.un.VietNam", null, locale));
		countryList.put("YE", resources.getMessage("label.un.Yemen", null, locale));
		countryList.put("ZM", resources.getMessage("label.un.Zambia", null, locale));
		countryList.put("ZW", resources.getMessage("label.un.Zimbabwe", null, locale));
		
		countryList = Tools.sortByComparator(countryList);
		return countryList;
	}
	
	@RequestMapping(value = "/euDGs", method = {RequestMethod.GET, RequestMethod.HEAD})
	public @ResponseBody Map<String, String> getListOfDGs(HttpServletRequest request) {
		Locale locale = new Locale(request.getParameter("lang"));
		
		Map<String, String> dgList = new HashMap<>();

		dgList.put("SG",resources.getMessage("label.dg.SG",null,locale));
		dgList.put("SJ",resources.getMessage("label.dg.SJ",null,locale));
		dgList.put("COMM",resources.getMessage("label.dg.COMM",null,locale));
		dgList.put("EPSC",resources.getMessage("label.dg.EPSC",null,locale));
		dgList.put("ECFIN",resources.getMessage("label.dg.ECFIN",null,locale));
		dgList.put("GROW",resources.getMessage("label.dg.GROW",null,locale));
		dgList.put("COMP",resources.getMessage("label.dg.COMP",null,locale));
		dgList.put("EMPL",resources.getMessage("label.dg.EMPL",null,locale));
		dgList.put("AGRI",resources.getMessage("label.dg.AGRI",null,locale));
		dgList.put("ENER",resources.getMessage("label.dg.ENER",null,locale));
		dgList.put("MOVE",resources.getMessage("label.dg.MOVE",null,locale));
		dgList.put("CLIMA",resources.getMessage("label.dg.CLIMA",null,locale));
		dgList.put("ENV",resources.getMessage("label.dg.ENV",null,locale));
		dgList.put("RTD",resources.getMessage("label.dg.RTD",null,locale));
		dgList.put("JRC",resources.getMessage("label.dg.JRC",null,locale));
		dgList.put("CNECT",resources.getMessage("label.dg.CNECT",null,locale));
		dgList.put("MARE",resources.getMessage("label.dg.MARE",null,locale));
		dgList.put("FISMA",resources.getMessage("label.dg.FISMA",null,locale));
		dgList.put("REGIO",resources.getMessage("label.dg.REGIO",null,locale));
		dgList.put("TAXUD",resources.getMessage("label.dg.TAXUD",null,locale));
		dgList.put("EAC",resources.getMessage("label.dg.EAC2",null,locale));
		dgList.put("SANTE",resources.getMessage("label.dg.SANTE",null,locale));
		dgList.put("HOME",resources.getMessage("label.dg.HOME",null,locale));
		dgList.put("JUST",resources.getMessage("label.dg.JUST",null,locale));
		dgList.put("FPI",resources.getMessage("label.dg.FPI",null,locale));
		dgList.put("TRADE",resources.getMessage("label.dg.TRADE",null,locale));
		dgList.put("NEAR",resources.getMessage("label.dg.NEAR",null,locale));
		dgList.put("DEVCO",resources.getMessage("label.dg.DEVCO",null,locale));
		dgList.put("ECHO",resources.getMessage("label.dg.ECHO",null,locale));
		dgList.put("ESTAT",resources.getMessage("label.dg.ESTAT",null,locale));
		dgList.put("HR",resources.getMessage("label.dg.HR",null,locale));
		dgList.put("DIGIT",resources.getMessage("label.dg.DIGIT",null,locale));
		dgList.put("BUDG",resources.getMessage("label.dg.BUDG",null,locale));
		dgList.put("IAS",resources.getMessage("label.dg.IAS",null,locale));
		dgList.put("OLAF",resources.getMessage("label.dg.OLAF",null,locale));
		dgList.put("SCIC",resources.getMessage("label.dg.SCIC",null,locale));
		dgList.put("DGT",resources.getMessage("label.dg.DGT",null,locale));
		dgList.put("OP",resources.getMessage("label.dg.OP",null,locale));
		dgList.put("OIB",resources.getMessage("label.dg.OIB",null,locale));
		dgList.put("PMO",resources.getMessage("label.dg.PMO",null,locale));
		dgList.put("OIL",resources.getMessage("label.dg.OIL",null,locale));
		dgList.put("EPSO",resources.getMessage("label.dg.EPSO",null,locale));
		dgList.put("EASME",resources.getMessage("label.dg.EASME",null,locale));
		dgList.put("EACEA",resources.getMessage("label.dg.EACEA",null,locale));
		dgList.put("CHAFEA",resources.getMessage("label.dg.CHAFEA",null,locale));
		dgList.put("INEA",resources.getMessage("label.dg.INEA",null,locale));
		dgList.put("ERCEA",resources.getMessage("label.dg.ERCEA",null,locale));
		dgList.put("REA",resources.getMessage("label.dg.REA",null,locale));		
		dgList.put("DPO",resources.getMessage("label.dg.DPO",null,locale));
		dgList.put("HAS",resources.getMessage("label.dg.HAS",null,locale));
		dgList.put("LIB",resources.getMessage("label.dg.LIB",null,locale));
		dgList.put("SRSS",resources.getMessage("label.dg.SRSS",null,locale));
		dgList.put("A50TF",resources.getMessage("label.dg.A50TF",null,locale));
			
		dgList = Tools.sortByComparator(dgList);
		return dgList;
	}
	
	@RequestMapping(value = "/config/{key}", method = {RequestMethod.GET, RequestMethod.HEAD})
	public @ResponseBody String getConfigValue(@PathVariable String key, HttpServletRequest request) {
		String result = "";		
		if(key != null)
		{
			if(key.equals("captchaBypass"))
			{				
				result =  bypassCaptcha;				
			}
		}
		return result;
	}
}