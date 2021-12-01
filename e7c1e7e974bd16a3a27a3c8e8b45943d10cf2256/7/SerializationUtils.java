/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the Apache License; either
 * version 2.0 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.griddynamics.jagger.util;

import biz.source_code.base64Coder.Base64Coder;
import com.griddynamics.jagger.exception.TechnicalException;
import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.jboss.serial.io.JBossObjectInputStream;
import org.jboss.serial.io.JBossObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

// TODO Avoid static code. Extract interface and make this one default implementation.
public class SerializationUtils {
    private static final Logger log = LoggerFactory.getLogger(SerializationUtils.class);
    private static final AtomicLong fromStringCount = new AtomicLong(0);
    private static final AtomicLong toStringCount = new AtomicLong(0);
    private static boolean useJBoss=true;//if false - using default java serialization

    private SerializationUtils() {
    }

    public static <T extends Serializable> T fromString(String s) {
        if (s.isEmpty()) {
            log.info("fromString({}, '{}')", fromStringCount.getAndIncrement(), s);
        }
        ObjectInputStream ois = null;
        try {
            byte[] data = Base64Coder.decode(s);
            try{
                //TODO fixes for support old reports
                ois=new JBossObjectInputStream(new ByteArrayInputStream(data));
            } catch (IOException e) {
                // /data stored not with JBoss
                ois=new ObjectInputStream(new ByteArrayInputStream(data));
            }
            T obj = (T) ois.readObject();
            return obj;
        } catch (IOException e) {
            log.error("Deserialization exception ", e);
            log.error("fromString('{}')", s);
            throw new TechnicalException(e);
        } catch (ClassNotFoundException e) {
            log.error("Deserialization exception ", e);
            throw new TechnicalException(e);
        } finally {
            try {
                Closeables.close(ois, true);
            } catch (IOException e) {
                log.warn("IOException should not have been thrown.", e);
            }
        }
    }

    public static String toString(Serializable o) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            if(useJBoss){
                oos=new JBossObjectOutputStream(baos);
            } else{
                baos = new ByteArrayOutputStream();
                oos=new ObjectOutputStream(baos);
            }
            oos.writeObject(o);
        } catch (IOException e) {
            log.error("Serialization exception ", e);
            throw new TechnicalException(e);
        } finally {
            String s = new String(Base64Coder.encode(baos.toByteArray()));
            if (s.isEmpty()) {
                log.info("toString({}, '{}', '{}')", toStringCount.getAndIncrement(), s, o);
            }
            try {
                Closeables.close(oos, true);
            } catch (IOException e) {
                log.warn("IOException should not have been thrown.", e);
            }
            return s;
        }
    }

    public static byte[] serialize(Object obj) {
        ObjectOutputStream ous = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if(useJBoss){
                ous=new JBossObjectOutputStream(baos);
            } else {
                baos = new ByteArrayOutputStream();
                ous=new ObjectOutputStream(baos);
            }
            ous.writeObject(obj);

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error during " + obj + " serialization", e);
        }  finally {
            try {
                Closeables.close(ous, true);
            } catch (IOException e) {
                log.warn("IOException should not have been thrown.", e);
            }
            try {
                Closeables.close(baos, true);
            } catch (IOException e) {
                log.warn("IOException should not have been thrown.", e);
            }
        }
    }

    public static Object deserialize(byte[] data) {
        return deserialize(data, SerializationUtils.class.getClassLoader());
    }
    
    public static Object deserialize(byte[] data, ClassLoader classLoader) {
        ObjectInputStream ois = null;
        try {
            try{
                //TODO fixes for support old reports
                ois= new JBossObjectInputStream(new ByteArrayInputStream(data), classLoader);
            } catch (IOException e){
                //data stored not with JBoss
                ois=new ClassLoaderObjectInputStream(classLoader, new ByteArrayInputStream(data));
            }
        
            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Closeables.close(ois, true);
            } catch (IOException e) {
                log.warn("IOException should not have been thrown.", e);
            }
        }
    }

    public static Configurator getConfigurator() {
        return Configurator.configuratorInstance;
    }

    public static class Configurator {
        private static final Configurator configuratorInstance = new Configurator();

        private Configurator() {}

        public void setUseJBoss(boolean useJBoss) {
            log.info("setting useJBoss= {}",useJBoss);
            SerializationUtils.useJBoss = useJBoss;
        }
    }
}
