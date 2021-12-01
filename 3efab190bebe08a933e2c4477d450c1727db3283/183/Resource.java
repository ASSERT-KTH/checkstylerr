/* 
 * Copyright 2015 Torridity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tor.tribes.types;

/**
 *
 * @author Torridity
 */
 public class Resource {

        public enum Type {

            WOOD, CLAY, IRON
        }
        private int amount = 0;
        private Type type;

        public Resource(int pAmount, Type pType) {
            setAmount(pAmount);
            setType(pType);
        }

        /**
         * @return the amount
         */
        public int getAmount() {
            return amount;
        }

        /**
         * @param amount the amount to set
         */
        public void setAmount(int amount) {
            this.amount = (amount > 0) ? amount : 0;
        }

        /**
         * @return the type
         */
        public Type getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(Type type) {
            this.type = type;
        }
    }
