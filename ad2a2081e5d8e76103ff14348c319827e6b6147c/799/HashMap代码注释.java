    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
	//Ŀ�ģ�����hashCodeֵ
    static final int hash(Object key) {
        int h;
		//���key��null��hash��0
		//���key��null����key��hashCodeֵ �� key��hashCodeֵ��16�������
		//		��������key��hashCodeֵ��16λ���16λ���������ĸ�������
		
		/*
		index = hash & table.length-1
		�����key��ԭʼ��hashCodeֵ  �� table.length-1 ���а�λ�룬��ô�����ϸ�16û�������ϡ�
		�����ͻ����ӳ�ͻ�ĸ��ʣ�Ϊ�˽��ͳ�ͻ�ĸ��ʣ��Ѹ�16λ���뵽hash��Ϣ�С�
		*/
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; //����
		Node<K,V> p; //һ�����
		int n, i;//n������ĳ���   i���±�
		
		//tab��table�ȼ�
		//���table�ǿյ�
        if ((tab = table) == null || (n = tab.length) == 0){
            //n = (tab = resize()).length;
			tab = resize();
			n = tab.length;
			/*
			���table�ǿյģ�resize()�����
			�ٴ�����һ������Ϊ16������
			��threshold = 12
			
			n= 16
			*/
        }
		//i = (n - 1) & hash ���±� = ���鳤��-1 & hash
		//p = tab[i] 
		//if(p==null) ��������Ļ�˵�� table[i]��û��Ԫ��
		if ((p = tab[i = (n - 1) & hash]) == null){
			//���µ�ӳ���ϵֱ�ӷ���table[i]
            tab[i] = newNode(hash, key, value, null);
			//newNode���������ʹ�����һ��Node���͵��½�㣬�½���next��null
        }else {
            Node<K,V> e; 
			K k;
			//p��table[i]�е�һ�����
			//if(table[i]�ĵ�һ��������µ�ӳ���ϵ��key�ظ�)
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k)))){
                e = p;//��e��¼���table[i]�ĵ�һ�����
			}else if (p instanceof TreeNode){//���table[i]��һ�������һ�������
                //�������������
				e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            }else {
				//table[i]�ĵ�һ����㲻������㣬Ҳ���µ�ӳ���ϵ��key���ظ�
				//binCount��¼��table[i]����Ľ��ĸ���
                for (int binCount = 0; ; ++binCount) {
					//���p����һ������ǿյģ�˵����ǰ��p�����һ�����
                    if ((e = p.next) == null) {
						//���µĽ�����ӵ�table[i]�����
                        p.next = newNode(hash, key, value, null);
						
						//���binCount>=8-1���ﵽ7��ʱ
                        if (binCount >= TREEIFY_THRESHOLD - 1){ // -1 for 1st
                            //Ҫô���ݣ�Ҫô����
							treeifyBin(tab, hash);
						}
                        break;
                    }
					//���key�ظ��ˣ�������forѭ��
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))){
                        break;
					}
                    p = e;
                }
            }
			//������e����null��˵����key�ظ����Ϳ����滻ԭ����value
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null){
                    e.value = value;
				}
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
		
		//Ԫ�ظ�������
		//size�ﵽ��ֵ
        if (++size > threshold){
            resize();//һ�����ݣ����µ�������ӳ���ϵ��λ��
		}
        afterNodeInsertion(evict);//ʲôҲû��
        return null;
    }	
	
   final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;//oldTabԭ����table
		//oldCap��ԭ������ĳ���
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
		
		//oldThr��ԭ������ֵ
        int oldThr = threshold;//�ʼthreshold��0
		
		//newCap��������
		//newThr������ֵ
        int newCap, newThr = 0;
        if (oldCap > 0) {//˵��ԭ�����ǿ�����
            if (oldCap >= MAXIMUM_CAPACITY) {//�Ƿ�ﵽ�����������
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY){
				//newCap = �ɵ�����*2 ��������<���������������
				//��������32,64��...
				//oldCap >= ��ʼ����16
				//����ֵ������ = 24��48 ....
                newThr = oldThr << 1; // double threshold
			}
        }else if (oldThr > 0){ // initial capacity was placed in threshold
            newCap = oldThr;
        }else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;//��������Ĭ�ϳ�ʼ������16
			//����ֵ= Ĭ�ϵļ������� * Ĭ�ϵĳ�ʼ������ = 0.75*16 = 12
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;//��ֵ��ֵΪ����ֵ12��24.������
		
		//������һ�������飬����ΪnewCap��16��32,64.����
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
		
		
        if (oldTab != null) {//ԭ�����ǿ�����
			//��ԭ����table��ӳ���ϵ�����ڵ��µ�table��
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {//e��table����Ľ��
                    oldTab[j] = null;//�Ѿɵ�table[j]λ�����
                    if (e.next == null)//��������һ�����
                        newTab[e.hash & (newCap - 1)] = e;//���¼���e������table�еĴ洢λ�ã�Ȼ�����
                    else if (e instanceof TreeNode)//���e�������
						//��ԭ��������⣬�ŵ��µ�table
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
						/*
						��ԭ��table[i]�����������������Ų�����µ�table��
						*/
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }	
	
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
		//����һ���½��
	   return new Node<>(hash, key, value, next);
    }

    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; 
		Node<K,V> e;
		//MIN_TREEIFY_CAPACITY����С��������64
		//���table�ǿյģ�����  table�ĳ���û�дﵽ64
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();//������
        else if ((e = tab[index = (n - 1) & hash]) != null) {
			//��e��¼table[index]�Ľ��ĵ�ַ
            TreeNode<K,V> hd = null, tl = null;
			/*
			do...while����table[index]�����Ϊ�����
			*/
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
			
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }	