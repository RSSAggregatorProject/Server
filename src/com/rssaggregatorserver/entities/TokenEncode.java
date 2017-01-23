package com.rssaggregatorserver.entities;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TokenEncode {

	public static String Encode(String str)
	{
		byte[]	tokenByte = str.getBytes(StandardCharsets.UTF_8);
		return (Base64.getEncoder().encodeToString(tokenByte));
	}
	
	public static String Decode(String str) throws UnsupportedEncodingException
	{
		byte[] bytes = Base64.getDecoder().decode(str);
		String tmp = new String(bytes, "UTF-8");
		return (tmp);
	}
}
